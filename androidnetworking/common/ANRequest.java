/*
 *    Copyright (C) 2016 Amit Shekhar
 *    Copyright (C) 2011 Android Open Source Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package onelinelibrary.com.onelinelibrary.main_module.androidnetworking.common;

import android.graphics.Bitmap;
import android.widget.ImageView;

import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.core.Core;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.error.ANError;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.BitmapRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.DownloadListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.DownloadProgressListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.JSONArrayRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.JSONObjectRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.OkHttpResponseAndBitmapRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.OkHttpResponseAndJSONArrayRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.OkHttpResponseListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.ParsedRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.StringRequestListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.UploadProgressListener;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.interfaces.progressListner;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.internal.ANRequestQueue;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.internal.SynchronousCall;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.utils.ParseUtil;
import onelinelibrary.com.onelinelibrary.main_module.androidnetworking.utils.Utils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Okio;

/**
 * Created by Dhruv Kaushal on 26/04/16.
 */
@SuppressWarnings({"unchecked", "unused"})
public class ANRequest<T extends ANRequest> {

    private final static String TAG = ANRequest.class.getSimpleName();

    private int mMethod;
    private Priority mPriority;
    private int mRequestType;
    private String mUrl;
    private int sequenceNumber;
    private Object mTag;
    private ResponseType mResponseType;
    private HashMap<String, String> mHeadersMap = new HashMap<>();
    private HashMap<String, String> mBodyParameterMap = new HashMap<>();
    private HashMap<String, String> mUrlEncodedFormBodyParameterMap = new HashMap<>();
    private HashMap<String, String> mMultiPartParameterMap = new HashMap<>();
    private HashMap<String, String> mQueryParameterMap = new HashMap<>();
    private HashMap<String, String> mPathParameterMap = new HashMap<>();
    private HashMap<String, File> mMultiPartFileMap = new HashMap<>();
    private String mDirPath;
    private String mFileName;
    private JSONObject mJsonObject = null;
    private JSONArray mJsonArray = null;
    private String mStringBody = null;
    private byte[] mByte = null;
    private File mFile = null;
    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_MARKDOWN =
            MediaType.parse("text/x-markdown; charset=utf-8");
    private MediaType customMediaType = null;
    private static final Object sDecodeLock = new Object();

    private Future future;
    private Call call;
    private int mProgress;
    private boolean isCancelled;
    private boolean isDelivered;
    private int mPercentageThresholdForCancelling = 0;
    private JSONArrayRequestListener mJSONArrayRequestListener;
    private JSONObjectRequestListener mJSONObjectRequestListener;
    private StringRequestListener mStringRequestListener;
    private OkHttpResponseListener mOkHttpResponseListener;
    private BitmapRequestListener mBitmapRequestListener;
    private ParsedRequestListener mParsedRequestListener;
    private OkHttpResponseAndJSONObjectRequestListener mOkHttpResponseAndJSONObjectRequestListener;
    private OkHttpResponseAndJSONArrayRequestListener mOkHttpResponseAndJSONArrayRequestListener;
    private OkHttpResponseAndStringRequestListener mOkHttpResponseAndStringRequestListener;
    private OkHttpResponseAndBitmapRequestListener mOkHttpResponseAndBitmapRequestListener;
    private OkHttpResponseAndParsedRequestListener mOkHttpResponseAndParsedRequestListener;
    private DownloadProgressListener mDownloadProgressListener;
    private UploadProgressListener mUploadProgressListener;
    private DownloadListener mDownloadListener;
    private progressListner mAnalyticsListener;

    private Bitmap.Config mDecodeConfig;
    private int mMaxWidth;
    private int mMaxHeight;
    private ImageView.ScaleType mScaleType;
    private CacheControl mCacheControl = null;
    private Executor mExecutor = null;
    private OkHttpClient mOkHttpClient = null;
    private String mUserAgent = null;
    private Type mType = null;

    public ANRequest(GetRequestBuilder builder) {
        this.mRequestType = RequestType.SIMPLE;
        this.mMethod = builder.mMethod;
        this.mPriority = builder.mPriority;
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mHeadersMap = builder.mHeadersMap;
        this.mDecodeConfig = builder.mDecodeConfig;
        this.mMaxHeight = builder.mMaxHeight;
        this.mMaxWidth = builder.mMaxWidth;
        this.mScaleType = builder.mScaleType;
        this.mQueryParameterMap = builder.mQueryParameterMap;
        this.mPathParameterMap = builder.mPathParameterMap;
        this.mCacheControl = builder.mCacheControl;
        this.mExecutor = builder.mExecutor;
        this.mOkHttpClient = builder.mOkHttpClient;
        this.mUserAgent = builder.mUserAgent;
    }

    public ANRequest(PostRequestBuilder builder) {
        this.mRequestType = RequestType.SIMPLE;
        this.mMethod = builder.mMethod;
        this.mPriority = builder.mPriority;
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mHeadersMap = builder.mHeadersMap;
        this.mBodyParameterMap = builder.mBodyParameterMap;
        this.mUrlEncodedFormBodyParameterMap = builder.mUrlEncodedFormBodyParameterMap;
        this.mQueryParameterMap = builder.mQueryParameterMap;
        this.mPathParameterMap = builder.mPathParameterMap;
        this.mJsonObject = builder.mJsonObject;
        this.mJsonArray = builder.mJsonArray;
        this.mStringBody = builder.mStringBody;
        this.mFile = builder.mFile;
        this.mByte = builder.mByte;
        this.mCacheControl = builder.mCacheControl;
        this.mExecutor = builder.mExecutor;
        this.mOkHttpClient = builder.mOkHttpClient;
        this.mUserAgent = builder.mUserAgent;
        if (builder.mCustomContentType != null) {
            this.customMediaType = MediaType.parse(builder.mCustomContentType);
        }
    }

    public ANRequest(DownloadBuilder builder) {
        this.mRequestType = RequestType.DOWNLOAD;
        this.mMethod = Method.GET;
        this.mPriority = builder.mPriority;
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mDirPath = builder.mDirPath;
        this.mFileName = builder.mFileName;
        this.mHeadersMap = builder.mHeadersMap;
        this.mQueryParameterMap = builder.mQueryParameterMap;
        this.mPathParameterMap = builder.mPathParameterMap;
        this.mCacheControl = builder.mCacheControl;
        this.mPercentageThresholdForCancelling = builder.mPercentageThresholdForCancelling;
        this.mExecutor = builder.mExecutor;
        this.mOkHttpClient = builder.mOkHttpClient;
        this.mUserAgent = builder.mUserAgent;
    }

    public ANRequest(MultiPartBuilder builder) {
        this.mRequestType = RequestType.MULTIPART;
        this.mMethod = Method.POST;
        this.mPriority = builder.mPriority;
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mHeadersMap = builder.mHeadersMap;
        this.mQueryParameterMap = builder.mQueryParameterMap;
        this.mPathParameterMap = builder.mPathParameterMap;
        this.mMultiPartParameterMap = builder.mMultiPartParameterMap;
        this.mMultiPartFileMap = builder.mMultiPartFileMap;
        this.mCacheControl = builder.mCacheControl;
        this.mPercentageThresholdForCancelling = builder.mPercentageThresholdForCancelling;
        this.mExecutor = builder.mExecutor;
        this.mOkHttpClient = builder.mOkHttpClient;
        this.mUserAgent = builder.mUserAgent;
        if (builder.mCustomContentType != null) {
            this.customMediaType = MediaType.parse(builder.mCustomContentType);
        }
    }

    public void getAsJSONObject(JSONObjectRequestListener requestListener) {
        this.mResponseType = ResponseType.JSON_OBJECT;
        this.mJSONObjectRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsJSONArray(JSONArrayRequestListener requestListener) {
        this.mResponseType = ResponseType.JSON_ARRAY;
        this.mJSONArrayRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsString(StringRequestListener requestListener) {
        this.mResponseType = ResponseType.STRING;
        this.mStringRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsOkHttpResponse(OkHttpResponseListener requestListener) {
        this.mResponseType = ResponseType.OK_HTTP_RESPONSE;
        this.mOkHttpResponseListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsBitmap(BitmapRequestListener requestListener) {
        this.mResponseType = ResponseType.BITMAP;
        this.mBitmapRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsParsed(TypeToken typeToken, ParsedRequestListener parsedRequestListener) {
        this.mType = typeToken.getType();
        this.mResponseType = ResponseType.PARSED;
        this.mParsedRequestListener = parsedRequestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsOkHttpResponseAndJSONObject(OkHttpResponseAndJSONObjectRequestListener requestListener) {
        this.mResponseType = ResponseType.JSON_OBJECT;
        this.mOkHttpResponseAndJSONObjectRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsOkHttpResponseAndJSONArray(OkHttpResponseAndJSONArrayRequestListener requestListener) {
        this.mResponseType = ResponseType.JSON_ARRAY;
        this.mOkHttpResponseAndJSONArrayRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsOkHttpResponseAndString(OkHttpResponseAndStringRequestListener requestListener) {
        this.mResponseType = ResponseType.STRING;
        this.mOkHttpResponseAndStringRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }


    public void getAsOkHttpResponseAndBitmap(OkHttpResponseAndBitmapRequestListener requestListener) {
        this.mResponseType = ResponseType.BITMAP;
        this.mOkHttpResponseAndBitmapRequestListener = requestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void getAsOkHttpResponseAndParsed(TypeToken typeToken, OkHttpResponseAndParsedRequestListener parsedRequestListener) {
        this.mType = typeToken.getType();
        this.mResponseType = ResponseType.PARSED;
        this.mOkHttpResponseAndParsedRequestListener = parsedRequestListener;
        ANRequestQueue.getInstance().addRequest(this);
    }


    public void startDownload(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public void prefetch() {
        this.mResponseType = ResponseType.PREFETCH;
        ANRequestQueue.getInstance().addRequest(this);
    }

    public ANResponse executeForJSONObject() {
        this.mResponseType = ResponseType.JSON_OBJECT;
        return SynchronousCall.execute(this);
    }

    public ANResponse executeForJSONArray() {
        this.mResponseType = ResponseType.JSON_ARRAY;
        return SynchronousCall.execute(this);
    }

    public ANResponse executeForString() {
        this.mResponseType = ResponseType.STRING;
        return SynchronousCall.execute(this);
    }

    public ANResponse executeForOkHttpResponse() {
        this.mResponseType = ResponseType.OK_HTTP_RESPONSE;
        return SynchronousCall.execute(this);
    }

    public ANResponse executeForBitmap() {
        this.mResponseType = ResponseType.BITMAP;
        return SynchronousCall.execute(this);
    }

    public ANResponse executeForParsed(TypeToken typeToken) {
        this.mType = typeToken.getType();
        this.mResponseType = ResponseType.PARSED;
        return SynchronousCall.execute(this);
    }

    public ANResponse executeForDownload() {
        return SynchronousCall.execute(this);
    }

    public T setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
        this.mDownloadProgressListener = downloadProgressListener;
        return (T) this;
    }

    public T setUploadProgressListener(UploadProgressListener uploadProgressListener) {
        this.mUploadProgressListener = uploadProgressListener;
        return (T) this;
    }

    public T progress(progressListner analyticsListener) {
        this.mAnalyticsListener = analyticsListener;
        return (T) this;
    }

    public progressListner getAnalyticsListener() {
        return mAnalyticsListener;
    }

    public int getMethod() {
        return mMethod;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public String getUrl() {
        String tempUrl = mUrl;
        for (HashMap.Entry<String, String> entry : mPathParameterMap.entrySet()) {
            tempUrl = tempUrl.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(tempUrl).newBuilder();
        for (HashMap.Entry<String, String> entry : mQueryParameterMap.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        return urlBuilder.build().toString();
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public void setResponseAs(ResponseType responseType) {
        this.mResponseType = responseType;
    }

    public ResponseType getResponseAs() {
        return mResponseType;
    }

    public Object getTag() {
        return mTag;
    }

    public int getRequestType() {
        return mRequestType;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public void setUserAgent(String userAgent) {
        this.mUserAgent = userAgent;
    }

    public String getUserAgent() {
        return mUserAgent;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        this.mType = type;
    }

    public DownloadProgressListener getDownloadProgressListener() {
        return new DownloadProgressListener() {
            @Override
            public void onProgress(final long bytesDownloaded, final long totalBytes) {
                if (mDownloadProgressListener != null && !isCancelled) {
                    mDownloadProgressListener.onProgress(bytesDownloaded, totalBytes);
                }
            }
        };
    }

    public void updateDownloadCompletion() {
        isDelivered = true;
        if (mDownloadListener != null) {
            if (!isCancelled) {
                if (mExecutor != null) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (mDownloadListener != null) {
                                mDownloadListener.onDownloadComplete();
                            }
                            ANLog.d("Delivering success : " + toString());
                            finish();
                        }
                    });
                } else {
                    Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (mDownloadListener != null) {
                                mDownloadListener.onDownloadComplete();
                            }
                            ANLog.d("Delivering success : " + toString());
                            finish();
                        }
                    });
                }
            } else {
                deliverError(new ANError());
                finish();
            }
        } else {
            ANLog.d("Prefetch done : " + toString());
            finish();
        }
    }

    public UploadProgressListener getUploadProgressListener() {
        return new UploadProgressListener() {
            @Override
            public void onProgress(final long bytesUploaded, final long totalBytes) {
                mProgress = (int) ((bytesUploaded * 100) / totalBytes);
                if (mUploadProgressListener != null && !isCancelled) {
                    mUploadProgressListener.onProgress(bytesUploaded, totalBytes);
                }
            }
        };
    }

    public String getDirPath() {
        return mDirPath;
    }

    public String getFileName() {
        return mFileName;
    }

    public CacheControl getCacheControl() {
        return mCacheControl;
    }

    public ImageView.ScaleType getScaleType() {
        return mScaleType;
    }

    public void cancel(boolean forceCancel) {
        try {
            if (forceCancel || mPercentageThresholdForCancelling == 0
                    || mProgress < mPercentageThresholdForCancelling) {
                ANLog.d("cancelling request : " + toString());
                isCancelled = true;
                if (call != null) {
                    call.cancel();
                }
                if (future != null) {
                    future.cancel(true);
                }
                if (!isDelivered) {
                    deliverError(new ANError());
                }
            } else {
                ANLog.d("not cancelling request : " + toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCanceled() {
        return isCancelled;
    }

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

    public void destroy() {
        mJSONArrayRequestListener = null;
        mJSONArrayRequestListener = null;
        mStringRequestListener = null;
        mBitmapRequestListener = null;
        mParsedRequestListener = null;
        mDownloadProgressListener = null;
        mUploadProgressListener = null;
        mDownloadListener = null;
        mAnalyticsListener = null;
    }

    public void finish() {
        destroy();
        ANRequestQueue.getInstance().finish(this);
    }

    public ANResponse parseResponse(Response response) {
        switch (mResponseType) {
            case JSON_ARRAY:
                try {
                    JSONArray json = new JSONArray(Okio.buffer(response.body().source()).readUtf8());
                    return ANResponse.success(json);
                } catch (Exception e) {
                    return ANResponse.failed(Utils.getErrorForParse(new ANError(e)));
                }
            case JSON_OBJECT:
                try {
                    JSONObject json = new JSONObject(Okio.buffer(response.body()
                            .source()).readUtf8());
                    return ANResponse.success(json);
                } catch (Exception e) {
                    return ANResponse.failed(Utils.getErrorForParse(new ANError(e)));
                }
            case STRING:
                try {
                    return ANResponse.success(Okio.buffer(response
                            .body().source()).readUtf8());
                } catch (Exception e) {
                    return ANResponse.failed(Utils.getErrorForParse(new ANError(e)));
                }
            case BITMAP:
                synchronized (sDecodeLock) {
                    try {
                        return Utils.decodeBitmap(response, mMaxWidth, mMaxHeight,
                                mDecodeConfig, mScaleType);
                    } catch (Exception e) {
                        return ANResponse.failed(Utils.getErrorForParse(new ANError(e)));
                    }
                }
            case PARSED:
                try {
                    return ANResponse.success(ParseUtil.getParserFactory()
                            .responseBodyParser(mType).convert(response.body()));
                } catch (Exception e) {
                    return ANResponse.failed(Utils.getErrorForParse(new ANError(e)));
                }
            case PREFETCH:
                return ANResponse.success(ANConstants.PREFETCH);
        }
        return null;
    }

    public ANError parseNetworkError(ANError anError) {
        try {
            if (anError.getResponse() != null && anError.getResponse().body() != null
                    && anError.getResponse().body().source() != null) {
                anError.setErrorBody(Okio.buffer(anError
                        .getResponse().body().source()).readUtf8());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return anError;
    }

    public synchronized void deliverError(ANError anError) {
        try {
            if (!isDelivered) {
                if (isCancelled) {
                    anError.setCancellationMessageInError();
                    anError.setErrorCode(0);
                }
                deliverErrorResponse(anError);
                ANLog.d("Delivering anError : " + toString());
            }
            isDelivered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void deliverResponse(final ANResponse response) {
        try {
            isDelivered = true;
            if (!isCancelled) {
                if (mExecutor != null) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            deliverSuccessResponse(response);
                        }
                    });
                } else {
                    Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
                        public void run() {
                            deliverSuccessResponse(response);
                        }
                    });
                }
                ANLog.d("Delivering success : " + toString());
            } else {
                ANError anError = new ANError();
                anError.setCancellationMessageInError();
                anError.setErrorCode(0);
                deliverErrorResponse(anError);
                finish();
                ANLog.d("Delivering cancelled : " + toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverSuccessResponse(ANResponse response) {
        if (mJSONObjectRequestListener != null) {
            mJSONObjectRequestListener.onResponse((JSONObject) response.getResult());
        } else if (mJSONArrayRequestListener != null) {
            mJSONArrayRequestListener.onResponse((JSONArray) response.getResult());
        } else if (mStringRequestListener != null) {
            mStringRequestListener.onResponse((String) response.getResult());
        } else if (mBitmapRequestListener != null) {
            mBitmapRequestListener.onResponse((Bitmap) response.getResult());
        } else if (mParsedRequestListener != null) {
            mParsedRequestListener.onResponse(response.getResult());
        } else if (mOkHttpResponseAndJSONObjectRequestListener != null) {
            mOkHttpResponseAndJSONObjectRequestListener.onResponse(response.getOkHttpResponse(), (JSONObject) response.getResult());
        } else if (mOkHttpResponseAndJSONArrayRequestListener != null) {
            mOkHttpResponseAndJSONArrayRequestListener.onResponse(response.getOkHttpResponse(), (JSONArray) response.getResult());
        } else if (mOkHttpResponseAndStringRequestListener != null) {
            mOkHttpResponseAndStringRequestListener.onResponse(response.getOkHttpResponse(), (String) response.getResult());
        } else if (mOkHttpResponseAndBitmapRequestListener != null) {
            mOkHttpResponseAndBitmapRequestListener.onResponse(response.getOkHttpResponse(), (Bitmap) response.getResult());
        } else if (mOkHttpResponseAndParsedRequestListener != null) {
            mOkHttpResponseAndParsedRequestListener.onResponse(response.getOkHttpResponse(), response.getResult());
        }
        finish();
    }

    private void deliverErrorResponse(ANError anError) {
        if (mJSONObjectRequestListener != null) {
            mJSONObjectRequestListener.onError(anError);
        } else if (mJSONArrayRequestListener != null) {
            mJSONArrayRequestListener.onError(anError);
        } else if (mStringRequestListener != null) {
            mStringRequestListener.onError(anError);
        } else if (mBitmapRequestListener != null) {
            mBitmapRequestListener.onError(anError);
        } else if (mParsedRequestListener != null) {
            mParsedRequestListener.onError(anError);
        } else if (mOkHttpResponseAndJSONObjectRequestListener != null) {
            mOkHttpResponseAndJSONObjectRequestListener.onError(anError);
        } else if (mOkHttpResponseAndJSONArrayRequestListener != null) {
            mOkHttpResponseAndJSONArrayRequestListener.onError(anError);
        } else if (mOkHttpResponseAndStringRequestListener != null) {
            mOkHttpResponseAndStringRequestListener.onError(anError);
        } else if (mOkHttpResponseAndBitmapRequestListener != null) {
            mOkHttpResponseAndBitmapRequestListener.onError(anError);
        } else if (mOkHttpResponseAndParsedRequestListener != null) {
            mOkHttpResponseAndParsedRequestListener.onError(anError);
        }else if (mDownloadListener != null) {
            mDownloadListener.onError(anError);
        }
    }

    public void deliverOkHttpResponse(final Response response) {
        try {
            isDelivered = true;
            if (!isCancelled) {
                if (mExecutor != null) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (mOkHttpResponseListener != null) {
                                mOkHttpResponseListener.onResponse(response);
                            }
                            finish();
                        }
                    });
                } else {
                    Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
                        public void run() {
                            if (mOkHttpResponseListener != null) {
                                mOkHttpResponseListener.onResponse(response);
                            }
                            finish();
                        }
                    });
                }
                ANLog.d("Delivering success : " + toString());
            } else {
                ANError anError = new ANError();
                anError.setCancellationMessageInError();
                anError.setErrorCode(0);
                if (mOkHttpResponseListener != null) {
                    mOkHttpResponseListener.onError(anError);
                }
                finish();
                ANLog.d("Delivering cancelled : " + toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestBody getRequestBody() {
        if (mJsonObject != null) {
            if (customMediaType != null) {
                return RequestBody.create(customMediaType, mJsonObject.toString());
            }
            return RequestBody.create(JSON_MEDIA_TYPE, mJsonObject.toString());
        } else if (mJsonArray != null) {
            if (customMediaType != null) {
                return RequestBody.create(customMediaType, mJsonArray.toString());
            }
            return RequestBody.create(JSON_MEDIA_TYPE, mJsonArray.toString());
        } else if (mStringBody != null) {
            if (customMediaType != null) {
                return RequestBody.create(customMediaType, mStringBody);
            }
            return RequestBody.create(MEDIA_TYPE_MARKDOWN, mStringBody);
        } else if (mFile != null) {
            if (customMediaType != null) {
                return RequestBody.create(customMediaType, mFile);
            }
            return RequestBody.create(MEDIA_TYPE_MARKDOWN, mFile);
        } else if (mByte != null) {
            if (customMediaType != null) {
                return RequestBody.create(customMediaType, mByte);
            }
            return RequestBody.create(MEDIA_TYPE_MARKDOWN, mByte);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            try {
                for (HashMap.Entry<String, String> entry : mBodyParameterMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                for (HashMap.Entry<String, String> entry : mUrlEncodedFormBodyParameterMap.entrySet()) {
                    builder.addEncoded(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return builder.build();
        }
    }

    public RequestBody getMultiPartRequestBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        try {
            for (HashMap.Entry<String, String> entry : mMultiPartParameterMap.entrySet()) {
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + entry.getKey() + "\""),
                        RequestBody.create(null, entry.getValue()));
            }
            for (HashMap.Entry<String, File> entry : mMultiPartFileMap.entrySet()) {
                String fileName = entry.getValue().getName();
                RequestBody fileBody = RequestBody.create(MediaType.parse(Utils.getMimeType(fileName)),
                        entry.getValue());
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + entry.getKey() + "\"; filename=\"" + fileName + "\""),
                        fileBody);
                if (customMediaType != null) {
                    builder.setType(customMediaType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public Headers getHeaders() {
        Headers.Builder builder = new Headers.Builder();
        try {
            for (HashMap.Entry<String, String> entry : mHeadersMap.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static class HeadRequestBuilder extends GetRequestBuilder {

        public HeadRequestBuilder(String url) {
            super(url, Method.HEAD);
        }
    }

    public static class GetRequestBuilder<T extends GetRequestBuilder> implements RequestBuilder {
        private Priority mPriority = Priority.MEDIUM;
        private int mMethod = Method.GET;
        private String mUrl;
        private Object mTag;
        private Bitmap.Config mDecodeConfig;
        private int mMaxWidth;
        private int mMaxHeight;
        private ImageView.ScaleType mScaleType;
        private HashMap<String, String> mHeadersMap = new HashMap<>();
        private HashMap<String, String> mQueryParameterMap = new HashMap<>();
        private HashMap<String, String> mPathParameterMap = new HashMap<>();
        private CacheControl mCacheControl;
        private Executor mExecutor;
        private OkHttpClient mOkHttpClient;
        private String mUserAgent;

        public GetRequestBuilder(String url) {
            this.mUrl = url;
            this.mMethod = Method.GET;
        }

        public GetRequestBuilder(String url, int method) {
            this.mUrl = url;
            this.mMethod = method;
        }

        @Override
        public T setPriority(Priority priority) {
            mPriority = priority;
            return (T) this;
        }

        @Override
        public T setTag(Object tag) {
            mTag = tag;
            return (T) this;
        }

        @Override
        public T addQueryParameter(String key, String value) {
            mQueryParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addQueryParameter(HashMap<String, String> queryParameterMap) {
            if (queryParameterMap != null) {
                for (HashMap.Entry<String, String> entry : queryParameterMap.entrySet()) {
                    mQueryParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T addPathParameter(String key, String value) {
            mPathParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(String key, String value) {
            mHeadersMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(HashMap<String, String> headerMap) {
            if (headerMap != null) {
                for (HashMap.Entry<String, String> entry : headerMap.entrySet()) {
                    mHeadersMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T doNotCacheResponse() {
            mCacheControl = new CacheControl.Builder().noStore().build();
            return (T) this;
        }

        @Override
        public T getResponseOnlyIfCached() {
            mCacheControl = CacheControl.FORCE_CACHE;
            return (T) this;
        }

        @Override
        public T getResponseOnlyFromNetwork() {
            mCacheControl = CacheControl.FORCE_NETWORK;
            return (T) this;
        }

        @Override
        public T setMaxAgeCacheControl(int maxAge, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxAge(maxAge, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setMaxStaleCacheControl(int maxStale, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxStale(maxStale, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setExecutor(Executor executor) {
            mExecutor = executor;
            return (T) this;
        }

        @Override
        public T setOkHttpClient(OkHttpClient okHttpClient) {
            mOkHttpClient = okHttpClient;
            return (T) this;
        }

        @Override
        public T setUserAgent(String userAgent) {
            mUserAgent = userAgent;
            return (T) this;
        }

        public T setBitmapConfig(Bitmap.Config bitmapConfig) {
            mDecodeConfig = bitmapConfig;
            return (T) this;
        }

        public T setBitmapMaxHeight(int maxHeight) {
            mMaxHeight = maxHeight;
            return (T) this;
        }

        public T setBitmapMaxWidth(int maxWidth) {
            mMaxWidth = maxWidth;
            return (T) this;
        }

        public T setImageScaleType(ImageView.ScaleType imageScaleType) {
            mScaleType = imageScaleType;
            return (T) this;
        }

        public ANRequest start() {
            return new ANRequest(this);
        }
    }

    public static class PutRequestBuilder extends PostRequestBuilder {

        public PutRequestBuilder(String url) {
            super(url, Method.PUT);
        }
    }

    public static class DeleteRequestBuilder extends PostRequestBuilder {

        public DeleteRequestBuilder(String url) {
            super(url, Method.DELETE);
        }
    }

    public static class PatchRequestBuilder extends PostRequestBuilder {

        public PatchRequestBuilder(String url) {
            super(url, Method.PATCH);
        }
    }

    public static class PostRequestBuilder<T extends PostRequestBuilder> implements RequestBuilder {

        private Priority mPriority = Priority.MEDIUM;
        private int mMethod = Method.POST;
        private String mUrl;
        private Object mTag;
        private JSONObject mJsonObject = null;
        private JSONArray mJsonArray = null;
        private String mStringBody = null;
        private byte[] mByte = null;
        private File mFile = null;
        private HashMap<String, String> mHeadersMap = new HashMap<>();
        private HashMap<String, String> mBodyParameterMap = new HashMap<>();
        private HashMap<String, String> mUrlEncodedFormBodyParameterMap = new HashMap<>();
        private HashMap<String, String> mQueryParameterMap = new HashMap<>();
        private HashMap<String, String> mPathParameterMap = new HashMap<>();
        private CacheControl mCacheControl;
        private Executor mExecutor;
        private OkHttpClient mOkHttpClient;
        private String mUserAgent;
        private String mCustomContentType;

        public PostRequestBuilder(String url) {
            this.mUrl = url;
            this.mMethod = Method.POST;
        }

        public PostRequestBuilder(String url, int method) {
            this.mUrl = url;
            this.mMethod = method;
        }

        @Override
        public T setPriority(Priority priority) {
            mPriority = priority;
            return (T) this;
        }

        @Override
        public T setTag(Object tag) {
            mTag = tag;
            return (T) this;
        }

        @Override
        public T addQueryParameter(String key, String value) {
            mQueryParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addQueryParameter(HashMap<String, String> queryParameterMap) {
            if (queryParameterMap != null) {
                for (HashMap.Entry<String, String> entry : queryParameterMap.entrySet()) {
                    mQueryParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T addPathParameter(String key, String value) {
            mPathParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(String key, String value) {
            mHeadersMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(HashMap<String, String> headerMap) {
            if (headerMap != null) {
                for (HashMap.Entry<String, String> entry : headerMap.entrySet()) {
                    mHeadersMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T doNotCacheResponse() {
            mCacheControl = new CacheControl.Builder().noStore().build();
            return (T) this;
        }

        @Override
        public T getResponseOnlyIfCached() {
            mCacheControl = CacheControl.FORCE_CACHE;
            return (T) this;
        }

        @Override
        public T getResponseOnlyFromNetwork() {
            mCacheControl = CacheControl.FORCE_NETWORK;
            return (T) this;
        }

        @Override
        public T setMaxAgeCacheControl(int maxAge, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxAge(maxAge, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setMaxStaleCacheControl(int maxStale, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxStale(maxStale, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setExecutor(Executor executor) {
            mExecutor = executor;
            return (T) this;
        }

        @Override
        public T setOkHttpClient(OkHttpClient okHttpClient) {
            mOkHttpClient = okHttpClient;
            return (T) this;
        }

        @Override
        public T setUserAgent(String userAgent) {
            mUserAgent = userAgent;
            return (T) this;
        }

        public T addBodyParameter(String key, String value) {
            mBodyParameterMap.put(key, value);
            return (T) this;
        }

        public T addUrlEncodeFormBodyParameter(String key, String value) {
            mUrlEncodedFormBodyParameterMap.put(key, value);
            return (T) this;
        }

        public T addBodyParameter(HashMap<String, String> bodyParameterMap) {
            if (bodyParameterMap != null) {
                for (HashMap.Entry<String, String> entry : bodyParameterMap.entrySet()) {
                    mBodyParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        public T addUrlEncodeFormBodyParameter(HashMap<String, String> bodyParameterMap) {
            if (bodyParameterMap != null) {
                for (HashMap.Entry<String, String> entry : bodyParameterMap.entrySet()) {
                    mUrlEncodedFormBodyParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        public T addJSONObjectBody(JSONObject jsonObject) {
            mJsonObject = jsonObject;
            return (T) this;
        }

        public T addJSONArrayBody(JSONArray jsonArray) {
            mJsonArray = jsonArray;
            return (T) this;
        }

        public T addStringBody(String stringBody) {
            mStringBody = stringBody;
            return (T) this;
        }

        public T addFileBody(File file) {
            mFile = file;
            return (T) this;
        }

        public T addByteBody(byte[] bytes) {
            mByte = bytes;
            return (T) this;
        }

        public T setContentType(String contentType) {
            mCustomContentType = contentType;
            return (T) this;
        }

        public ANRequest build() {
            return new ANRequest(this);
        }
    }

    public static class DownloadBuilder<T extends DownloadBuilder> implements RequestBuilder {

        private Priority mPriority = Priority.MEDIUM;
        private String mUrl;
        private Object mTag;
        private HashMap<String, String> mHeadersMap = new HashMap<>();
        private HashMap<String, String> mQueryParameterMap = new HashMap<>();
        private HashMap<String, String> mPathParameterMap = new HashMap<>();
        private String mDirPath;
        private String mFileName;
        private CacheControl mCacheControl;
        private int mPercentageThresholdForCancelling = 0;
        private Executor mExecutor;
        private OkHttpClient mOkHttpClient;
        private String mUserAgent;

        public DownloadBuilder(String url, String dirPath, String fileName) {
            this.mUrl = url;
            this.mDirPath = dirPath;
            this.mFileName = fileName;
        }

        @Override
        public T setPriority(Priority priority) {
            mPriority = priority;
            return (T) this;
        }

        @Override
        public T setTag(Object tag) {
            mTag = tag;
            return (T) this;
        }

        @Override
        public T addHeaders(String key, String value) {
            mHeadersMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(HashMap<String, String> headerMap) {
            if (headerMap != null) {
                for (HashMap.Entry<String, String> entry : headerMap.entrySet()) {
                    mHeadersMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T addQueryParameter(String key, String value) {
            mQueryParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addQueryParameter(HashMap<String, String> queryParameterMap) {
            if (queryParameterMap != null) {
                for (HashMap.Entry<String, String> entry : queryParameterMap.entrySet()) {
                    mQueryParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T addPathParameter(String key, String value) {
            mPathParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T doNotCacheResponse() {
            mCacheControl = new CacheControl.Builder().noStore().build();
            return (T) this;
        }

        @Override
        public T getResponseOnlyIfCached() {
            mCacheControl = CacheControl.FORCE_CACHE;
            return (T) this;
        }

        @Override
        public T getResponseOnlyFromNetwork() {
            mCacheControl = CacheControl.FORCE_NETWORK;
            return (T) this;
        }

        @Override
        public T setMaxAgeCacheControl(int maxAge, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxAge(maxAge, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setMaxStaleCacheControl(int maxStale, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxStale(maxStale, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setExecutor(Executor executor) {
            mExecutor = executor;
            return (T) this;
        }

        @Override
        public T setOkHttpClient(OkHttpClient okHttpClient) {
            mOkHttpClient = okHttpClient;
            return (T) this;
        }

        @Override
        public T setUserAgent(String userAgent) {
            mUserAgent = userAgent;
            return (T) this;
        }

        public T setPercentageThresholdForCancelling(int percentageThresholdForCancelling) {
            mPercentageThresholdForCancelling = percentageThresholdForCancelling;
            return (T) this;
        }

        public ANRequest build() {
            return new ANRequest(this);
        }
    }

    public static class MultiPartBuilder<T extends MultiPartBuilder> implements RequestBuilder {

        private Priority mPriority = Priority.MEDIUM;
        private String mUrl;
        private Object mTag;
        private HashMap<String, String> mHeadersMap = new HashMap<>();
        private HashMap<String, String> mMultiPartParameterMap = new HashMap<>();
        private HashMap<String, String> mQueryParameterMap = new HashMap<>();
        private HashMap<String, String> mPathParameterMap = new HashMap<>();
        private HashMap<String, File> mMultiPartFileMap = new HashMap<>();
        private CacheControl mCacheControl;
        private int mPercentageThresholdForCancelling = 0;
        private Executor mExecutor;
        private OkHttpClient mOkHttpClient;
        private String mUserAgent;
        private String mCustomContentType;

        public MultiPartBuilder(String url) {
            this.mUrl = url;
        }

        @Override
        public T setPriority(Priority priority) {
            mPriority = priority;
            return (T) this;
        }

        @Override
        public T setTag(Object tag) {
            mTag = tag;
            return (T) this;
        }

        @Override
        public T addQueryParameter(String key, String value) {
            mQueryParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addQueryParameter(HashMap<String, String> queryParameterMap) {
            if (queryParameterMap != null) {
                for (HashMap.Entry<String, String> entry : queryParameterMap.entrySet()) {
                    mQueryParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T addPathParameter(String key, String value) {
            mPathParameterMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(String key, String value) {
            mHeadersMap.put(key, value);
            return (T) this;
        }

        @Override
        public T addHeaders(HashMap<String, String> headerMap) {
            if (headerMap != null) {
                for (HashMap.Entry<String, String> entry : headerMap.entrySet()) {
                    mHeadersMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        @Override
        public T doNotCacheResponse() {
            mCacheControl = new CacheControl.Builder().noStore().build();
            return (T) this;
        }

        @Override
        public T getResponseOnlyIfCached() {
            mCacheControl = CacheControl.FORCE_CACHE;
            return (T) this;
        }

        @Override
        public T getResponseOnlyFromNetwork() {
            mCacheControl = CacheControl.FORCE_NETWORK;
            return (T) this;
        }

        @Override
        public T setMaxAgeCacheControl(int maxAge, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxAge(maxAge, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setMaxStaleCacheControl(int maxStale, TimeUnit timeUnit) {
            mCacheControl = new CacheControl.Builder().maxStale(maxStale, timeUnit).build();
            return (T) this;
        }

        @Override
        public T setExecutor(Executor executor) {
            mExecutor = executor;
            return (T) this;
        }

        @Override
        public T setOkHttpClient(OkHttpClient okHttpClient) {
            mOkHttpClient = okHttpClient;
            return (T) this;
        }

        @Override
        public T setUserAgent(String userAgent) {
            mUserAgent = userAgent;
            return (T) this;
        }

        public T addMultipartParameter(String key, String value) {
            mMultiPartParameterMap.put(key, value);
            return (T) this;
        }

        public T addMultipartParameter(HashMap<String, String> multiPartParameterMap) {
            if (multiPartParameterMap != null) {
                for (HashMap.Entry<String, String> entry : multiPartParameterMap.entrySet()) {
                    mMultiPartParameterMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        public T parameter(String key, File file) {
            mMultiPartFileMap.put(key, file);
            return (T) this;
        }

        public T addMultipartFile(HashMap<String, File> multiPartFileMap) {
            if (multiPartFileMap != null) {
                for (HashMap.Entry<String, File> entry : multiPartFileMap.entrySet()) {
                    mMultiPartFileMap.put(entry.getKey(), entry.getValue());
                }
            }
            return (T) this;
        }

        public T setPercentageThresholdForCancelling(int percentageThresholdForCancelling) {
            this.mPercentageThresholdForCancelling = percentageThresholdForCancelling;
            return (T) this;
        }

        public T setContentType(String contentType) {
            mCustomContentType = contentType;
            return (T) this;
        }

        public ANRequest start() {
            return new ANRequest(this);
        }
    }

    @Override
    public String toString() {
        return "ANRequest{" +
                "sequenceNumber='" + sequenceNumber +
                ", mMethod=" + mMethod +
                ", mPriority=" + mPriority +
                ", mRequestType=" + mRequestType +
                ", mUrl=" + mUrl +
                '}';
    }
}
