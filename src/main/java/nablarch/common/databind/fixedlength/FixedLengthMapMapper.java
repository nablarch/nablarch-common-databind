package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.fixedlength.FixedLengthReader.Record;
import nablarch.core.util.FileUtil;

/**
 * 固定長をMapにマッピングする{@link ObjectMapper}
 *
 * @author MAENO Daisuke.
 */
public class FixedLengthMapMapper extends FixedLengthMapperSupport {

    public FixedLengthMapMapper(FixedLengthDataBindConfig config, InputStream stream) {
        super(null, config, stream);
    }

    @Override
    Object createObject(Class clazz, Map fields) {
        // レコード情報がそのままMapに格納されているため、そのまま返す
        return fields;
    }
}
