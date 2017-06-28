package nablarch.common.databind.fixedlength;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.DataBindConfigConverter;
import nablarch.common.databind.DataBindUtil;
import nablarch.core.beans.BeanUtil;

/**
 * {@link FixedLength}アノテーションを{@link FixedLengthDataBindConfig}に変換するクラス。
 *
 * @author siosio
 */
public class FixedLengthDataBindConfigConverter implements DataBindConfigConverter<FixedLength> {

    @Override
    public DataBindConfig convert(final Class<?> beanClass) {
        final FixedLength fixedLength = beanClass.getAnnotation(FixedLength.class);

        return FixedLengthDataBindConfigBuilder.newBuilder()
                                               .length(fixedLength.length())
                                               .charset(Charset.forName(fixedLength.charset()))
                                               .lineSeparator(fixedLength.lineSeparator())
                                               .addRecord(createRecordConfig(beanClass))
                                               .build();

    }

    /**
     * レコードの定義を生成する。
     *
     * @param beanClass レコードの定義を生成するBean
     * @return レコードの定義
     */
    private RecordConfig createRecordConfig(final Class<?> beanClass) {
        final PropertyDescriptor[] descriptors = BeanUtil.getPropertyDescriptors(beanClass);
        final List<FieldConfig> fieldDefinitions = new ArrayList<FieldConfig>(descriptors.length);

        for (final PropertyDescriptor descriptor : descriptors) {
            final Method method = descriptor.getReadMethod();
            final Field field = method.getAnnotation(Field.class);
            if (field == null) {
                continue;
            }

            final FieldConverterConfig fieldConverterConfig = getFieldConverter(descriptor);
            fieldDefinitions.add(
                    new FieldConfig(descriptor.getName(), field.offset(), field.length(), fieldConverterConfig));
        }

        Collections.sort(fieldDefinitions, new FieldConfigComparator());
        return new RecordConfig(fieldDefinitions);
    }

    /**
     * 設定されているフィールドコンバータを返す。
     *
     * @param propertyDescriptor 対象のフィールド
     * @return フィールドコンバータ
     */
    private FieldConverterConfig getFieldConverter(final PropertyDescriptor propertyDescriptor) {
        FieldConverterConfig fieldConverterConfig = null;
        for (final Annotation annotation : propertyDescriptor.getReadMethod().getAnnotations()) {
            final FieldConvert fieldConvert = annotation.annotationType()
                                                        .getAnnotation(FieldConvert.class);
            if (fieldConvert != null) {
                if (fieldConverterConfig != null) {
                    throw new IllegalStateException("multiple field converters can not be set. field_name:" + propertyDescriptor.getName());
                }
                fieldConverterConfig = new FieldConverterConfig(annotation,
                        DataBindUtil.newInstance(fieldConvert.value()));
            }
        }
        return fieldConverterConfig;
    }

    @Override
    public Class<FixedLength> getType() {
        return FixedLength.class;
    }

    /**
     * フィールドのオフセットを基準に比較を行うクラス。
     */
    @SuppressWarnings("ComparatorNotSerializable")
    private static class FieldConfigComparator implements Comparator<FieldConfig> {

        @Override
        public int compare(final FieldConfig o1, final FieldConfig o2) {
            final int first = o1.getOffset();
            final int second = o2.getOffset();
            if (first < second) {
                return -1;
            } else if (first > second) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
