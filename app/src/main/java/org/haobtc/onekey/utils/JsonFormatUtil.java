package org.haobtc.onekey.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Iterator;
import org.json.JSONException;

/**
 * Json 字符串格式化
 *
 * @author Onekey@QuincySx
 * @create 2021-04-02 11:02 AM
 */
public class JsonFormatUtil {
    public static String format(final JsonElement object) throws JSONException {
        final JsonVisitor visitor = new JsonVisitor(4, ' ');
        visitor.visit(object, 0);
        return visitor.toString();
    }

    private static class JsonVisitor {

        private final StringBuilder builder = new StringBuilder();
        private final int indentationSize;
        private final char indentationChar;

        public JsonVisitor(final int indentationSize, final char indentationChar) {
            this.indentationSize = indentationSize;
            this.indentationChar = indentationChar;
        }

        private void visit(final JsonArray array, final int indent) throws JSONException {
            final int length = array.size();
            if (length == 0) {
                write("\n" /* [] */, indent);
            } else {
                // write("" /* [ */, indent);
                for (int i = 0; i < length; i++) {
                    visit(array.get(i), indent + 1);
                }
                // write("" /* ] */, indent);
            }
        }

        private void visit(final JsonObject obj, final int indent) throws JSONException {
            final int length = obj.size();
            if (length == 0) {
                write("\n" /* {} */, indent);
            } else {
                // write("" /* { */, indent);
                final Iterator<String> keys = obj.keySet().iterator();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    write(key + " :", indent + 1);
                    visit(obj.get(key), indent + 1);
                    if (keys.hasNext()) {
                        write("" /* , */, indent + 1);
                    }
                }
                // write("" /* } */, indent);
            }
        }

        private void visit(final Object object, final int indent) throws JSONException {
            if (object instanceof JsonArray) {
                visit((JsonArray) object, indent);
            } else if (object instanceof JsonObject) {
                visit((JsonObject) object, indent);
            } else {
                if (object instanceof String) {
                    // write("\"" + (String) object + "\"", indent);
                    write((String) object, indent);
                } else {
                    write(String.valueOf(object), indent);
                }
            }
        }

        private void write(final String data, final int indent) {
            for (int i = 0; i < (indent * indentationSize); i++) {
                builder.append(indentationChar);
            }
            builder.append(data).append('\n');
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
