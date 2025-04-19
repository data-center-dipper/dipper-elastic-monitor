package com.dipper.monitor.support;

import org.springframework.context.ApplicationContext;

public interface ContextStartListener {
    public void onStart(ApplicationContext ac);
}
