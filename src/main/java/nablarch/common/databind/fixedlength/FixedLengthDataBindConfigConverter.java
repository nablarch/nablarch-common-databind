package nablarch.common.databind.fixedlength;

import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.DataBindConfigConverter;
import nablarch.common.databind.DataBindUtil;
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

        final FixedLengthDataBindConfigBuilder builder = FixedLengthDataBindConfigBuilder.newBuilder()
                .length(fixedLength.length())
                .charset(Charset.forName(fixedLength.charset()))
                .lineSeparator(fixedLength.lineSeparator())
                .fillChar(fixedLength.fillChar());

        if (fixedLength.multiLayout()) {
            if (!MultiLayout.class.isAssignableFrom(beanClass)) {
                throw new IllegalStateException("bean class must inherit " + MultiLayout.class.getName() + ". bean_class:" + beanClass.getName());
            }

            builder.multiLayout(new MultiLayoutConfig(((MultiLayout) DataBindUtil.newInstance(beanClass)).getRecordIdentifier()));

            for (java.lang.reflect.Field field : beanClass.getDeclaredFields()) {

                final Record record = field.getAnnotation(Record.class);
                if (record != null) {
                    builder.addRecord(createRecordConfig(field.getType(), field.getName()));
                }
            }
        } else {
            builder.addRecord(createRecordConfig(beanClass, RecordConfig.SINGLE_LAYOUT_RECORD_NAME));
        }

        return builder.build();
    }

    /**
     * レコードの定義を生成する。
     *
     * @param beanClass レコードの定義を生成するBean
     * @param recordName レコード名
     * @return レコードの定義
     */
    private RecordConfig createRecordConfig(final Class<?> beanClass, final String recordName) {
        final RecordBuilder recordBuilder = new RecordBuilder();
        for (final java.lang.reflect.Field field : beanClass.getDeclaredFields()) {
            final Field fieldAnnotation = field.getAnnotation(Field.class);
            if (fieldAnnotation == null) {
                continue;
            }

            final FieldConvert.FieldConverter fieldConverter = getFieldConverter(field);
            if (fieldConverter == null) {
                recordBuilder.addField(field.getName(), fieldAnnotation.offset(), fieldAnnotation.length());
            } else {
                recordBuilder.addField(field.getName(), fieldAnnotation.offset(), fieldAnnotation.length(), fieldConverter);
            }

        }
        return recordBuilder.recordName(recordName).build();
    }

    /**
     * 設定されているフィールドコンバータを返す。
     *
     * @param field 対象のフィールド
     * @return フィールドコンバータ
     */
    @SuppressWarnings("rawtypes")
    private FieldConvert.FieldConverter getFieldConverter(final java.lang.reflect.Field field) {
        FieldConvert.FieldConverter fieldConverter = null;
        for (final Annotation annotation : field.getDeclaredAnnotations()) {
            final FieldConvert fieldConvert = annotation.annotationType()
                                                        .getAnnotation(FieldConvert.class);

            if (fieldConvert != null) {
                if (fieldConverter != null) {
                    throw new IllegalStateException("multiple field converters can not be set. field_name:" + field.getName());
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
