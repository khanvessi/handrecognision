//#include <jni.h>
//#include <string>
//#include <jni.h>
//#include <time.h>
//#include <android/log.h>
//#include <android/bitmap.h>
//
//#include <stdio.h>
//#include <stdlib.h>
//#include <math.h>
//#include <iostream>
//#include <jni.h>
//
//
//extern "C" JNIEXPORT jstring  extern "C" jstring
//Java_com_example_imagefilters_MainActivity_stringFromJNI(
//        JNIEnv *env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}
//
////
////
////static void contrast(int width, int height, int *pixels, float value) {
////
////    float red, green, blue;
////    int R, G, B;
////
////    for (int i = 0; i < width * height; i++) {
////        red = (pixels[i] >> 16) & 0xFF;
////        green = (pixels[i] >> 8) & 0xFF;
////        blue = (pixels[i]) & 0xFF;
////
////        red = (((((red / 255.0) - 0.5) * value) + 0.5) * 255.0);
////        green = (((((green / 255.0) - 0.5) * value) + 0.5) * 255.0);
////        blue = (((((blue / 255.0) - 0.5) * value) + 0.5) * 255.0);
////
////        // validation check
////        if (red > 255)
////            red = 255;
////        else if (red < 0)
////            red = 0;
////
////        if (green > 255)
////            green = 255;
////        else if (green < 0)
////            green = 0;
////
////        if (blue > 255)
////            blue = 255;
////        else if (blue < 0)
////            blue = 0;
////
////        R = (int) red;
////        G = (int) green;
////        B = (int) blue;
////        pixels[i] = (pixels[i] & 0xFF000000) | ((R << 16) & 0x00FF0000) | ((G << 8) & 0x0000FF00) | (B & 0x000000FF);
////    }
////}
////
//
//
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_example_imagefilters_MainActivity_blurTheImage(JNIEnv *env, jobject thiz, jstring img) {
//    Mat src, dst;
//    float sum;
//
//    /// Load an image
//    src = imread(img, CV_LOAD_IMAGE_GRAYSCALE);
//
//    if (!src.data) { return -1; }
//
//    // define the kernel
//    float Kernel[3][3] = {
//            {1 / 9.0, 1 / 9.0, 1 / 9.0},
//            {1 / 9.0, 1 / 9.0, 1 / 9.0},
//            {1 / 9.0, 1 / 9.0, 1 / 9.0}
//    };
//    dst = src.clone();
//    for (int y = 0; y < src.rows; y++)
//        for (int x = 0; x < src.cols; x++)
//            dst.at<uchar>(y, x) = 0.0;
//    //convolution operation
//    for (int y = 1; y < src.rows - 1; y++) {
//        for (int x = 1; x < src.cols - 1; x++) {
//            sum = 0.0;
//            for (int k = -1; k <= 1; k++) {
//                for (int j = -1; j <= 1; j++) {
//                    sum = sum + Kernel[j + 1][k + 1] * src.at<uchar>(y - j, x - k);
//                }
//            }
//            dst.at<uchar>(y, x) = sum;
//        }
//    }
//
//
//
////    namedWindow("final");
////    imshow("final", dst);
////
////    namedWindow("initial");
////    imshow("initial", src);
//
//    //waitKey();
//}
