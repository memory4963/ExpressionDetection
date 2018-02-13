//
// Created by 10733 on 2018/2/2.
//

#include "MoveDetection.h"

MoveDetection::MoveDetection(){
    bgSubtractor = createBackgroundSubtractorMOG2(30, 36, false);
}

void MoveDetection::loadImage(Mat frame) {
//    bgSubtractor()
}
