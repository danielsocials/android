package org.haobtc.onekey.manager;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import okhttp3.Authenticator;
import okhttp3.CacheControl;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/** @Description: 构建OkHttp添加代理 @Author: peter Qin */
public class OkHttpWebViewClient extends WebViewClient {

    private static final String HOST = "cdn.onekey.so";
    private static final int PORT = 443;
    private static final String USER_NAME = "onekey";
    private static final String PASS = "libbitcoinconsensus";

    private static final String TAG = OkHttpWebViewClient.class.getSimpleName();

    private static final boolean TRUST_ALL_CERTIFICATES = false;

    private OkHttpClient client;

    private onReceivePageStatus mOnReceivePageStatus;

    public OkHttpWebViewClient(onReceivePageStatus onReceivePageStatus) {
        this.mOnReceivePageStatus = onReceivePageStatus;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return loadResponse(view, url);
    }

    @SuppressLint("NewApi")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return loadResponse(view, request.getUrl().toString());
    }

    private WebResourceResponse loadResponse(WebView view, String url) {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    initClient();
                }
            }
        }
        // suppress favicon requests as we don't display them anywhere
        if (url.endsWith("/favicon.ico")) {
            return new WebResourceResponse("image/png", null, null);
        }

        try {
            final Request okReq =
                    new Request.Builder()
                            .url(url)
                            .cacheControl(new CacheControl.Builder().noCache().build())
                            .build();

            final long startMillis = System.currentTimeMillis();
            final Response okResp = client.newCall(okReq).execute();
            final long dtMillis = System.currentTimeMillis() - startMillis;
            return okHttpResponseToWebResourceResponse(okResp);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Convert OkHttp {@link Response} into a {@link WebResourceResponse}
     *
     * @param resp The OkHttp {@link Response}
     * @return The {@link WebResourceResponse}
     */
    private WebResourceResponse okHttpResponseToWebResourceResponse(Response resp) {
        final String contentTypeValue = resp.header("Content-Type");
        if (contentTypeValue != null) {
            if (contentTypeValue.indexOf("charset=") > 0) {
                final String[] contentTypeAndEncoding = contentTypeValue.split("; ");
                final String contentType = contentTypeAndEncoding[0];
                final String charset = contentTypeAndEncoding[1].split("=")[1];
                return new WebResourceResponse(contentType, charset, resp.body().byteStream());
            } else {
                return new WebResourceResponse(contentTypeValue, null, resp.body().byteStream());
            }
        } else {
            return new WebResourceResponse(
                    "application/octet-stream", null, resp.body().byteStream());
        }
    }

    private void initClient() {

        OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS);
        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HOST, PORT)));
        builder.proxyAuthenticator(
                new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        // 设置代理服务器账号密码
                        String credential = Credentials.basic(USER_NAME, PASS);
                        return response.request()
                                .newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                });
        client = builder.build();
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mOnReceivePageStatus.onPageStarted();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mOnReceivePageStatus.onPageFinished();
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        mOnReceivePageStatus.onReceivedError();
    }

    public interface onReceivePageStatus {
        void onPageStarted();

        void onPageFinished();

        void onReceivedError();
    }
}
