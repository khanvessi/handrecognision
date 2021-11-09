package com.example.imagefilters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.imagefilters.databinding.ActivityMainBinding
import java.io.File
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.FileInputStream
import java.lang.Exception
import java.util.*

class MainActivity() : Activity(), View.OnTouchListener,
    CvCameraViewListener2 {
    companion object {
        private val TAG = "HandPose::MainActivity"
        val JAVA_DETECTOR = 0
        val NATIVE_DETECTOR = 1

        init {
            System.loadLibrary("imagefilters")

            OpenCVLoader.initDebug()

            if(OpenCVLoader.initDebug()){
                Log.e(TAG, "OPENCV SUCCESS: ", )
            }else{
                Log.e(TAG, "OPENCV FAILED: ", )
            }
        }
    }

    private var mRgba: Mat? = null
    private var mGray: Mat? = null
    private var mIntermediateMat: Mat? = null
    private val mDetectorType = JAVA_DETECTOR
    private var mOpenCvCameraView: CustomSufaceView? = null
    private val mResolutionList: List<Size>? = null
    private var minTresholdSeekbar: SeekBar? = null
    private val maxTresholdSeekbar: SeekBar? = null
    private var minTresholdSeekbarText: TextView? = null
    private var numberOfFingersText: TextView? = null
    var iThreshold = 0.0
    private var mBlobColorHsv: Scalar? = null
    private var mBlobColorRgba: Scalar? = null
    private var mDetector: ColorBlobDetector? = null
    private var mSpectrum: Mat? = null
    private var mIsColorSelected = false
    private var SPECTRUM_SIZE: Size? = null
    private var CONTOUR_COLOR: Scalar? = null
    private var CONTOUR_COLOR_WHITE: Scalar? = null
    val mHandler = Handler()
    var numberOfFingers = 0
    val mUpdateFingerCountResults: Runnable = Runnable { updateNumberOfFingers() }
    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        @SuppressLint("ClickableViewAccessibility")
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.e(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView?.enableView()
                    mOpenCvCameraView?.setOnTouchListener(this@MainActivity)
                }
                else -> {
                    Log.e(TAG, "OpenCV: FAILED", )
                    super.onManagerConnected(status)
                }
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.main_surface_view)
        if (!OpenCVLoader.initDebug()) {
            Log.e("Test", "man")
        } else {
        }

        System.loadLibrary("imagefilters")

        mOpenCvCameraView = findViewById<View>(R.id.main_surface_view) as CustomSufaceView
        mOpenCvCameraView!!.setCvCameraViewListener(this)
        minTresholdSeekbarText = findViewById<View>(R.id.textView3) as TextView
        numberOfFingersText = findViewById<View>(R.id.numberOfFingers) as TextView
        minTresholdSeekbar = findViewById<View>(R.id.seekBar1) as SeekBar
        minTresholdSeekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progressChanged = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                progressChanged = progress
                minTresholdSeekbarText!!.text = progressChanged.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                minTresholdSeekbarText!!.text = progressChanged.toString()
            }
        })
        minTresholdSeekbar!!.progress = 8700
    }

    public override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()
    }

    public override fun onResume() {
        super.onResume()
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
//        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
//        mOpenCvCameraView?.enableView()

    }

    public override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mGray = Mat()
        mRgba = Mat()
        mIntermediateMat = Mat()

        /*
        mResolutionList = mOpenCvCameraView.getResolutionList();
        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            Log.i(TAG, "Resolution Option ["+Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString()+"]");
        }

        Size resolution = mResolutionList.get(7);
        mOpenCvCameraView.setResolution(resolution);
        resolution = mOpenCvCameraView.getResolution();
        String caption = "Resolution "+ Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
        Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        */
        val resolution: Camera.Size? = mOpenCvCameraView?.getResolution()
        val caption =
            "Resolution " + resolution?.let { Integer.valueOf(it.width).toString() } + "x" + resolution?.let {
                Integer.valueOf(
                    it.height
                ).toString()
            }
        Toast.makeText(this, caption, Toast.LENGTH_SHORT).show()
        val cParams: Camera.Parameters? = mOpenCvCameraView?.getParameters()
        if (cParams != null) {
            cParams.focusMode = Camera.Parameters.FOCUS_MODE_INFINITY
        }
        mOpenCvCameraView?.setParameters(cParams)
        if (cParams != null) {
            Toast.makeText(this, "Focus mode : " + cParams.focusMode, Toast.LENGTH_SHORT).show()
        }
        mRgba = Mat(height, width, CvType.CV_8UC4)
        mDetector = ColorBlobDetector()
        mSpectrum = Mat()
        mBlobColorRgba = Scalar(255.0)
        mBlobColorHsv = Scalar(255.0)
        SPECTRUM_SIZE = Size(200.0, 64.0)
        CONTOUR_COLOR = Scalar(255.0, 0.0, 0.0, 255.0)
        CONTOUR_COLOR_WHITE = Scalar(255.0, 255.0, 255.0, 255.0)
    }

    override fun onCameraViewStopped() {
        mGray!!.release()
        mRgba!!.release()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val cols = mRgba!!.cols()
        val rows = mRgba!!.rows()
        val xOffset: Int? = (mOpenCvCameraView?.getWidth()?.minus(cols))?.div(2)
        val yOffset: Int? = (mOpenCvCameraView?.getHeight()?.minus(rows))?.div(2)
        val x = event.x.toInt() - xOffset!!
        val y = event.y.toInt() - yOffset!!
        Log.i(TAG, "Touch image coordinates: ($x, $y)")
        if (x < 0 || y < 0 || x > cols || y > rows) return false
        val touchedRect = Rect()
        touchedRect.x = if (x > 5) x - 5 else 0
        touchedRect.y = if (y > 5) y - 5 else 0
        touchedRect.width = if (x + 5 < cols) x + 5 - touchedRect.x else cols - touchedRect.x
        touchedRect.height = if (y + 5 < rows) y + 5 - touchedRect.y else rows - touchedRect.y
        val touchedRegionRgba = mRgba!!.submat(touchedRect)
        val touchedRegionHsv = Mat()
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv)
        val pointCount = touchedRect.width * touchedRect.height
        for (i in mBlobColorHsv?.`val`?.indices!!) mBlobColorHsv?.`val`?.set(i,
            pointCount.toDouble()
        )
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv)
        Log.i(
            TAG,
            "Touched rgba color: (" + mBlobColorRgba!!.`val`[0] + ", " + mBlobColorRgba!!.`val`[1] +
                    ", " + mBlobColorRgba!!.`val`[2] + ", " + mBlobColorRgba!!.`val`[3] + ")"
        )
        mDetector?.setHsvColor(mBlobColorHsv!!)
        Imgproc.resize(mDetector?.getSpectrum(), mSpectrum, SPECTRUM_SIZE)
        mIsColorSelected = true
        touchedRegionRgba.release()
        touchedRegionHsv.release()
        return false // don't need subsequent touch events
    }

    private fun converScalarHsv2Rgba(hsvColor: Scalar?): Scalar {
        val pointMatRgba = Mat()
        val pointMatHsv = Mat(1, 1, CvType.CV_8UC3, hsvColor)
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4)
        return Scalar(pointMatRgba[0, 0])
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat? {
        mRgba = inputFrame.rgba()
        mGray = inputFrame.gray()
        iThreshold = minTresholdSeekbar!!.progress.toDouble()

        //Imgproc.blur(mRgba, mRgba, new Size(5,5));
        Imgproc.GaussianBlur(mRgba, mRgba, Size(3.0, 3.0), 1.0, 1.0)
        //Imgproc.medianBlur(mRgba, mRgba, 3);
        if (!mIsColorSelected) return mRgba as Mat?
        val contours: MutableList<MatOfPoint> = mDetector?.getContours() as MutableList<MatOfPoint>
        mDetector?.process(mRgba)
        Log.d(TAG, "Contours count: " + contours.size)
        if (contours.size <= 0) {
            return mRgba
        }
        var rect = Imgproc.minAreaRect(MatOfPoint2f(*contours[0].toArray()))
        var boundWidth = rect.size.width
        var boundHeight = rect.size.height
        var boundPos = 0
        for (i in 1 until contours.size) {
            rect = Imgproc.minAreaRect(MatOfPoint2f(*contours[i].toArray()))
            if (rect.size.width * rect.size.height > boundWidth * boundHeight) {
                boundWidth = rect.size.width
                boundHeight = rect.size.height
                boundPos = i
            }
        }
        val boundRect = Imgproc.boundingRect(MatOfPoint(*contours[boundPos].toArray()))
        Imgproc.rectangle(mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR_WHITE, 2, 8, 0)
        Log.d(
            TAG,
            " Row start [" +
                    boundRect.tl().y.toInt() + "] row end [" +
                    boundRect.br().y.toInt() + "] Col start [" +
                    boundRect.tl().x.toInt() + "] Col end [" +
                    boundRect.br().x.toInt() + "]"
        )
        val rectHeightThresh = 0
        var a = boundRect.br().y - boundRect.tl().y
        a = a * 0.7
        a = boundRect.tl().y + a
        Log.d(
            TAG,
            " A [" + a + "] br y - tl y = [" + (boundRect.br().y - boundRect.tl().y) + "]"
        )

        //Core.rectangle( mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR, 2, 8, 0 );
        Imgproc.rectangle(mRgba, boundRect.tl(), Point(boundRect.br().x, a), CONTOUR_COLOR, 2, 8, 0)
        val pointMat = MatOfPoint2f()
        Imgproc.approxPolyDP(MatOfPoint2f(*contours[boundPos].toArray()), pointMat, 3.0, true)
        contours[boundPos] = MatOfPoint(*pointMat.toArray())
        val hull = MatOfInt()
        val convexDefect = MatOfInt4()
        Imgproc.convexHull(MatOfPoint(*contours[boundPos].toArray()), hull)
        if (hull.toArray().size < 3) return mRgba
        Imgproc.convexityDefects(MatOfPoint(*contours[boundPos].toArray()), hull, convexDefect)
        val hullPoints: MutableList<MatOfPoint> = LinkedList()
        val listPo: MutableList<Point> = LinkedList()
        for (j in hull.toList().indices) {
            listPo.add(contours[boundPos].toList()[hull.toList()[j]])
        }
        val e = MatOfPoint()
        e.fromList(listPo)
        hullPoints.add(e)
        val defectPoints: MutableList<MatOfPoint> = LinkedList()
        val listPoDefect: MutableList<Point> = LinkedList()
        var j = 0
        while (j < convexDefect.toList().size) {
            val farPoint = contours[boundPos].toList()[convexDefect.toList()[j + 2]]
            val depth = convexDefect.toList()[j + 3]
            if (depth > iThreshold && farPoint.y < a) {
                listPoDefect.add(contours[boundPos].toList()[convexDefect.toList()[j + 2]])
            }
            Log.d(TAG, "defects [" + j + "] " + convexDefect.toList()[j + 3])
            j = j + 4
        }
        val e2 = MatOfPoint()
        e2.fromList(listPo)
        defectPoints.add(e2)
        Log.d(TAG, "hull: " + hull.toList())
        Log.d(TAG, "defects: " + convexDefect.toList())
        Imgproc.drawContours(mRgba, hullPoints, -1, CONTOUR_COLOR, 3)
        val defectsTotal = convexDefect.total().toInt()
        Log.d(TAG, "Defect total $defectsTotal")
        numberOfFingers = listPoDefect.size
        if (numberOfFingers > 5) numberOfFingers = 5
        mHandler.post(mUpdateFingerCountResults)
        for (p: Point? in listPoDefect) {
            Imgproc.circle(mRgba, p, 6, Scalar(255.0, 0.0, 255.0))
        }
        return mRgba
    }

    private fun updateNumberOfFingers() {
        numberOfFingersText!!.text = numberOfFingers.toString()
    }

    init {
        Log.i(TAG, "Instantiated new " + this.javaClass)
    }
}