package com.jiggag.rnkakaomaps;

import android.os.Bundle;
import android.view.Choreographer;
import android.view.View;
import android.widget.FrameLayout;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.ArrayList;
import java.util.Map;

public class KakaoMapManager extends ViewGroupManager<FrameLayout> {

    public static final String REACT_CLASS = "KakaoMapView";
    public final int COMMAND_CREATE = 1;

    ReactApplicationContext reactContext;

    public KakaoMapManager(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public FrameLayout createViewInstance(ThemedReactContext reactContext) {
        return new FrameLayout(reactContext);
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("create", COMMAND_CREATE);
    }

    @Override
    public void receiveCommand(
            @NonNull FrameLayout root,
            String commandId,
            @Nullable ReadableArray args
    ) {
        super.receiveCommand(root, commandId, args);
        int reactNativeViewId = args.getInt(0);
        int commandIdInt = Integer.parseInt(commandId);

        switch (commandIdInt) {
            case COMMAND_CREATE:
                createFragment(root, reactNativeViewId);
                break;
            default: {}
        }
    }

    private ArrayList markerList;
    private double paramLat = Constants.INIT_LAT;
    private double paramLng = Constants.INIT_LNG;
    private String markerImageName;
    private String markerImageUrl;

    @ReactProp(name = "markerList")
    public void setMarkerList(FrameLayout view, @Nullable ReadableArray _markerList) {
        if (_markerList != null) {
            markerList = _markerList.toArrayList();
        }
    }

    @ReactProp(name = "centerPoint")
    public void setCenterPoint(FrameLayout view, @Nullable ReadableMap centerPoint) {
        if (centerPoint != null) {
            paramLat = centerPoint.getDouble(Constants.PARAM_LAT);
            paramLng = centerPoint.getDouble(Constants.PARAM_LNG);
        }
    }

    @ReactProp(name = "markerImageName")
    public void setMarkerImageName(FrameLayout view, @Nullable String _markerImageName) {
        markerImageName = _markerImageName;
    }

    @ReactProp(name = "markerImageUrl")
    public void setMarkerImageUrl(FrameLayout view, @Nullable String _markerImageUrl) {
        markerImageUrl = _markerImageUrl;
    }

    public void createFragment(FrameLayout root, int reactNativeViewId) {
        ViewGroup parentView = (ViewGroup) root.findViewById(reactNativeViewId);
        setupLayout(parentView);

        final KakaoMapFragment fragment = new KakaoMapFragment();
        FragmentActivity activity = (FragmentActivity) reactContext.getCurrentActivity();

        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.PARAM_LAT, paramLat);
        bundle.putDouble(Constants.PARAM_LNG, paramLng);
        bundle.putString(Constants.PARAM_MARKER_NAME, markerImageName);
        bundle.putString(Constants.PARAM_MARKER_IMAGE_URL, markerImageUrl);
        bundle.putParcelableArrayList(Constants.PARAM_MARKER_LIST, markerList);

        fragment.setArguments(bundle);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(reactNativeViewId, fragment, String.valueOf(reactNativeViewId))
                .commit();
    }

    public void setupLayout(View view) {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                manuallyLayoutChildren(view);
                view.getViewTreeObserver().dispatchOnGlobalLayout();
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    public void manuallyLayoutChildren(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY));

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }
}
