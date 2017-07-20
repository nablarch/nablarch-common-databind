package nablarch.common.databind.fixedlength;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.DataBindConfigConverter;
import nablarch.common.databind.DataBindUtil;
import nablarch.core.beans.BeanUtil;
import nablarch.core.beans.BeansException;

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

        final RecordBuilder recordBuilder = new RecordBuilder();
        for (final PropertyDescriptor descriptor : descriptors) {
            final Method method = descriptor.getReadMethod();
            final Field field = method.getAnnotation(Field.class);
            if (field == null) {
                continue;
            }

            final FieldConvert.FieldConverter fieldConverter = getFieldConverter(descriptor);
            if (fieldConverter == null) {
                recordBuilder.addField(descriptor.getName(), field.offset(), field.length());
            } else {
                recordBuilder.addField(descriptor.getName(), field.offset(), field.length(), fieldConverter);
            }

        }
        return recordBuilder.build();
    }

    /**
     * 設定されているフィールドコンバータを返す。
     *
     * @param propertyDescriptor 対象のフィールド
     * @return フィールドコンバータ
     */
    private FieldConvert.FieldConverter getFieldConverter(final PropertyDescriptor propertyDescriptor) {
        FieldConvert.FieldConverter fieldConverter = null;
        for (final Annotation annotation : propertyDescriptor.getReadMethod().getAnnotations()) {
            final FieldConvert fieldConvert = annotation.annotationType()
                                                        .getAnnotation(FieldConvert.class);

            if (fieldConvert != null) {
                if (fieldConverter != null) {
                    throw new IllegalStateException("multiple field converters can not be set. field_name:" + propertyDescriptor.getName());
                }

                try {
                    fieldConverter = DataBindUtil.newInstance(fieldConvert.value());
                } catch (BeansException e) {
                    throw new IllegalStateException("instance creation failed. class:" + fieldConvert.value().getName(), e);
                }
                fieldConverter.initialize(annotation);
            }
        }
        return fieldConverter;
    }

    @Override
    public Class<FixedLength> getType() {
        return FixedLength.class;
    }


}
