package org.haobtc.onekey.onekeys.dappbrowser;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.haobtc.onekey.BuildConfig;
import org.haobtc.onekey.R;

public class JsInjectorClient {

    private static final String DEFAULT_CHARSET = "utf-8";
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String JS_TAG_TEMPLATE =
            "<script type=\"text/javascript\">%1$s</script><script type=\"text/javascript\">%2$s</script>";

    private static final String DEF_RPC_URL =
            "https://ropsten.infura.io/v3/f001ce716b6e4a33a557f74df6fe8eff";

    private final Context context;
    private final OkHttpClient httpClient;

    private String jsLibrary;

    private int chainId = 1;
    private String walletAddress;
    // Note: this default RPC is overriden before injection
    private String rpcUrl = DEF_RPC_URL;

    public JsInjectorClient(Context context) {
        this.context = context;
        this.httpClient = createHttpClient();
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String address) {
        this.walletAddress = address;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    @Nullable
    JsInjectorResponse loadUrl(final String url, final Map<String, String> headers) {
        Request request = buildRequest(url, headers);
        JsInjectorResponse result = null;
        try {
            Response response = httpClient.newCall(request).execute();
            result = buildResponse(response);
        } catch (Exception ex) {
            Log.d("REQUEST_ERROR", "", ex);
        }
        return result;
    }

    String assembleJs(Context context, String template) {
        if (TextUtils.isEmpty(jsLibrary)) {
            jsLibrary = loadFile(context, R.raw.onekeywallet_min);
        }
        String initJs = loadInitJs(context);
        return String.format(template, jsLibrary, initJs);
    }

    @Nullable
    private JsInjectorResponse buildResponse(Response response) {
        String body = null;
        int code = response.code();
        try {
            if (response.isSuccessful()) {
                body = response.body().string();
            }
        } catch (IOException ex) {
            Log.d("READ_BODY_ERROR", "Ex", ex);
        }
        Request request = response.request();
        Response prior = response.priorResponse();
        boolean isRedirect = prior != null && prior.isRedirect();
        String result = injectJS(body);
        String contentType = getContentTypeHeader(response);
        String charset = getCharset(contentType);
        String mime = getMimeType(contentType);
        String finalUrl = request.url().toString();
        return new JsInjectorResponse(result, code, finalUrl, mime, charset, isRedirect);
    }

    String injectJS(String html) {
        String js = assembleJs(context, JS_TAG_TEMPLATE);
        return injectJS(html, js);
    }

    String injectJSAtEnd(String view, String newCode) {
        int position = getEndInjectionPosition(view);
        if (position >= 0) {
            String beforeTag = view.substring(0, position);
            String afterTab = view.substring(position);
            return beforeTag + newCode + afterTab;
        }
        return view;
    }

    String injectJS(String html, String js) {
        if (TextUtils.isEmpty(html)) {
            return html;
        }
        int position = getInjectionPosition(html);
        if (position >= 0) {
            String beforeTag = html.substring(0, position);
            String afterTab = html.substring(position);
            return beforeTag + js + afterTab;
        }
        return html;
    }

    private int getInjectionPosition(String body) {
        body = body.toLowerCase();
        int ieDetectTagIndex = body.indexOf("<!--[if");
        int scriptTagIndex = body.indexOf("<script");

        int index;
        if (ieDetectTagIndex < 0) {
            index = scriptTagIndex;
        } else {
            index = Math.min(scriptTagIndex, ieDetectTagIndex);
        }
        if (index < 0) {
            index = body.indexOf("</head");
        }
        if (index < 0) {
            index = 0; // just wrap whole view
        }
        return index;
    }

    private int getEndInjectionPosition(String body) {
        body = body.toLowerCase();
        int firstIndex = body.indexOf("<script");
        int nextIndex = body.indexOf("web3", firstIndex);
        return body.indexOf("</script", nextIndex);
    }

    @Nullable
    private Request buildRequest(String url, Map<String, String> headers) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            return null;
        }
        Request.Builder requestBuilder = new Request.Builder().get().url(httpUrl);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            requestBuilder.addHeader(key, headers.get(key));
        }
        return requestBuilder.build();
    }

    private String loadInitJs(Context context) {
        String initSrc = loadFile(context, R.raw.init);
        String address =
                walletAddress == null ? "0000000000000000000000000000000000000000" : walletAddress;
        return String.format(initSrc, address, rpcUrl, chainId, BuildConfig.DEBUG);
    }

    String injectStyleAndWrap(String view, String style) {
        if (style == null) {
            style = "";
        }
        // String injectHeader = "<head><meta name=\"viewport\" content=\"width=device-width,
        // user-scalable=false\" /></head>";
        String injectHeader =
                "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no\" />"; // iOS uses these header settings
        style =
                "<style type=\"text/css\">\n"
                        + style
                        + ".token-card {\n"
                        + "padding: 0pt;\n"
                        + "margin: 0pt;\n"
                        + "}</style></head>"
                        + "<body>\n";
        // the opening of the following </div> is in injectWeb3TokenInit();
        return injectHeader + style + view + "</div></body>";
    }

    private static String loadFile(Context context, @RawRes int rawRes) {
        byte[] buffer = new byte[0];
        try {
            InputStream in = context.getResources().openRawResource(rawRes);
            buffer = new byte[in.available()];
            int len = in.read(buffer);
            if (len < 1) {
                throw new IOException("Nothing is read.");
            }
        } catch (Exception ex) {
            Log.d("READ_JS_TAG", "Ex", ex);
        }
        return new String(buffer);
    }

    private String getMimeType(String contentType) {
        Matcher regexResult = Pattern.compile("^.*(?=;)").matcher(contentType);
        if (regexResult.find()) {
            return regexResult.group();
        }
        return DEFAULT_MIME_TYPE;
    }

    private String getCharset(String contentType) {
        Matcher regexResult = Pattern.compile("charset=([a-zA-Z0-9-]+)").matcher(contentType);
        if (regexResult.find()) {
            if (regexResult.groupCount() >= 2) {
                return regexResult.group(1);
            }
        }
        return DEFAULT_CHARSET;
    }

    @Nullable
    private String getContentTypeHeader(Response response) {
        Headers headers = response.headers();
        String contentType;
        if (TextUtils.isEmpty(headers.get("Content-Type"))) {
            if (TextUtils.isEmpty(headers.get("content-Type"))) {
                contentType = "text/data; charset=utf-8";
            } else {
                contentType = headers.get("content-Type");
            }
        } else {
            contentType = headers.get("Content-Type");
        }
        if (contentType != null) {
            contentType = contentType.trim();
        }
        return contentType;
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new WebViewCookieJar())
                .build();
    }
}
