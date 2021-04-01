package org.haobtc.onekey.utils.internet;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import org.haobtc.onekey.utils.OKHttpUtils;

public class ProgressResponseBody extends ResponseBody {

    public static final int UPDATE = 0x01;
    public static final String TAG = ProgressResponseBody.class.getName();
    private ResponseBody responseBody;
    private OKHttpUtils.ProgressListener mListener;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody body, OKHttpUtils.ProgressListener listener) {
        responseBody = body;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {

        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(mySource(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source mySource(Source source) {

        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                mListener.onProgress(
                        totalBytesRead, contentLength(), totalBytesRead == contentLength());
                return bytesRead;
            }
        };
    }
}
