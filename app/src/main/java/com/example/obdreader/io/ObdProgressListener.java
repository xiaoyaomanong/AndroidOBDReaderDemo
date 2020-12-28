package com.example.obdreader.io;

public interface ObdProgressListener {

    void stateUpdate(final ObdCommandJob job);

}