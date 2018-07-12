package com.dragosholban.androidfacedetection;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import static com.dragosholban.androidfacedetection.VideoFaceDetectionActivity.context;

class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private Boolean playing = false;
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        Paint p = new Paint();
        VideoFaceDetectionActivity.mediaPlayer = new MediaPlayer();
                // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        canvas.drawText("Left Eye" + face.getIsLeftEyeOpenProbability(), 0, canvas.getHeight()/4, mIdPaint);
        canvas.drawText("Right Eye" + face.getIsRightEyeOpenProbability(), 0, canvas.getHeight(), mIdPaint);
        if(((face.getIsRightEyeOpenProbability()<0.4) && (face.getIsRightEyeOpenProbability()> -1.0))&&((face.getIsLeftEyeOpenProbability()<0.4)&& (face.getIsLeftEyeOpenProbability()>-1.0))){
            p.setColor(Color.RED);
            p.setTextSize(280);
            canvas.drawText("SLEEPY", 0,canvas.getHeight()/2, p);
            try{
                if(playing){
//                    VideoFaceDetectionActivity.mediaPlayer.stop();
//                    playing = false;
                }else {
                    playing = true;
                    AssetFileDescriptor as = context.getAssets().openFd("alarm.wav");
                    VideoFaceDetectionActivity.mediaPlayer.setDataSource(as.getFileDescriptor(), as.getStartOffset(), as.getLength());
                    as.close();
                    VideoFaceDetectionActivity.mediaPlayer.prepare();
                    VideoFaceDetectionActivity.mediaPlayer.start();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playing = false;
                        }
                    }, 1500);
                }

            }catch (Exception e){

            };
        }

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        // Draws a circle for each face feature detected
//        for (Landmark landmark : face.getLandmarks()) {
//            // the preview display of front-facing cameras is flipped horizontally
//            float cx = canvas.getWidth() - scaleX(landmark.getPosition().x);
//            float cy = scaleY(landmark.getPosition().y);
//            canvas.drawCircle(cx, cy, 10, mIdPaint);
//        }
    }
}
