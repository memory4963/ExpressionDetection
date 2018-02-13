//
// Created by 10733 on 2018/2/2.
//

#ifndef EXPRESSIONDETECTION_MOVEDETECTION_H
#define EXPRESSIONDETECTION_MOVEDETECTION_H


#include <opencv2/core/hal/interface.h>
#include <opencv2/core/core.hpp>
#include <opencv2/core/hal/interface.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/video/background_segm.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/video/background_segm.hpp>

using namespace cv;

class MoveDetection {

public:
    Mat bgMat;
    Ptr<BackgroundSubtractorMOG2> bgSubtractor;

    MoveDetection();

    void loadImage(Mat frame);

};


#endif //EXPRESSIONDETECTION_MOVEDETECTION_H
