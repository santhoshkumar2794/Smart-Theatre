package com.santhosh.smarttheatre;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

/**
 * Created by santhosh-3366 on 07/02/17.
 */

public class MovieActivity extends AppCompatActivity {

    private static final int ANIM_DURATION = 500;
    int mLeftDelta;
    int mTopDelta;
    float mWidthScale;
    float mHeightScale;
    private ColorDrawable mBackground;
    private int thumbnailTop;
    private int thumbnailLeft;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private int statusBarColor;
    private int currentStatusBarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_activity);

        statusBarColor = getWindow().getStatusBarColor();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("extra");
        thumbnailTop = bundle.getInt("PROPNAME_SCREENLOCATION_TOP");
        thumbnailLeft = bundle.getInt("PROPNAME_SCREENLOCATION_LEFT");
        thumbnailWidth = bundle.getInt("PROPNAME_WIDTH");
        thumbnailHeight = bundle.getInt("PROPNAME_HEIGHT");

        MovieData movieData = (MovieData) bundle.getSerializable("movieData");

        final ImageView poster_imageView = (ImageView) findViewById(R.id.holder_image);
        final ImageView backdrop_imageView = (ImageView) findViewById(R.id.backdrop_image);

        TextView textView = (TextView) findViewById(R.id.title_view);
        TextView overViewContainer = (TextView) findViewById(R.id.overview);
        TextView release_date = (TextView) findViewById(R.id.release_date);
        TextView rating_view = (TextView) findViewById(R.id.rating_view);

        String poster_url = "https://image.tmdb.org/t/p/w342/" + movieData.poster_path;
        String backdrop_url = "https://image.tmdb.org/t/p/w500/" + movieData.backdrop_path;

        Picasso.with(this).load(poster_url).into(poster_imageView);
        Picasso.with(this).load(backdrop_url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                backdrop_imageView.setImageBitmap(bitmap);
                Palette.from(bitmap).setRegion(0, 0, bitmap.getWidth(), 100).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        Palette.Swatch swatch = getMostPopulousSwatch(palette);
                        if (swatch != null) {
                            boolean isLight = isLight(statusBarColor);
                            currentStatusBarColor = scrimify(swatch.getRgb(), !isLight, 0.075f);
                            Log.e("isLight", "here " + isLight + "  " + isLight(currentStatusBarColor));
                            if (isLight(currentStatusBarColor)) {
                                setLightStatusBar(backdrop_imageView);
                            }
                            ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), statusBarColor, currentStatusBarColor);
                            valueAnimator.setDuration(400);
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    getWindow().setStatusBarColor((int) valueAnimator.getAnimatedValue());
                                }
                            });
                            valueAnimator.start();
                        }
                    }
                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        textView.setText(movieData.original_title);
        overViewContainer.setText(movieData.overview);
        release_date.setText(movieData.release_date);
        rating_view.setText(String.valueOf(movieData.vote_average));
        ((RatingBar) findViewById(R.id.rating_bar)).setRating(movieData.vote_average / 2);

        mBackground = new ColorDrawable(Color.WHITE);
        findViewById(R.id.top_level_view).setBackground(mBackground);
        if (savedInstanceState == null) {
            ViewTreeObserver observer = poster_imageView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    poster_imageView.getViewTreeObserver().removeOnPreDrawListener(this);

                    int[] screenLocation = new int[2];
                    poster_imageView.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    mWidthScale = (float) thumbnailWidth / poster_imageView.getWidth();
                    mHeightScale = (float) thumbnailHeight / poster_imageView.getHeight();

                    runEnterAnimation();

                    return true;
                }
            });
        }
    }

    public static void setLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static
    @Nullable
    Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }

    private boolean isLight(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        return hsl[2] > 0.5f;
    }

    public static int scrimify(int color, boolean isDark, float lightnessMultiplier) {
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);

        if (!isDark) {
            lightnessMultiplier += 1f;
        } else {
            lightnessMultiplier = 1f - lightnessMultiplier;
        }

        hsl[2] = Math.max(0f, Math.min(1f, hsl[2] * lightnessMultiplier));
        return android.support.v4.graphics.ColorUtils.HSLToColor(hsl);
    }

    public void runEnterAnimation() {
        final long duration = (long) (700);

        final ImageView poster_imageView = (ImageView) findViewById(R.id.holder_image);
        poster_imageView.setPivotX(0);
        poster_imageView.setPivotY(0);
        poster_imageView.setScaleX(mWidthScale);
        poster_imageView.setScaleY(mHeightScale);
        poster_imageView.setTranslationX(mLeftDelta);
        poster_imageView.setTranslationY(mTopDelta);

        poster_imageView.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(new DecelerateInterpolator());

        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        findViewById(R.id.backdrop_image).startAnimation(animation);
        findViewById(R.id.content_panel).startAnimation(animation);


        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();
    }

    public void runExitAnimation(final Runnable endAction) {
        final long duration = (long) 700;
        final ImageView poster_imageView = (ImageView) findViewById(R.id.holder_image);

        if (getResources().getConfiguration().orientation != 1) {
            poster_imageView.setPivotX(poster_imageView.getWidth() / 2);
            poster_imageView.setPivotY(poster_imageView.getHeight() / 2);
            mLeftDelta = 0;
            mTopDelta = 0;
        }

        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(duration / 2);
        animation.setFillAfter(true);
        findViewById(R.id.backdrop_image).startAnimation(animation);
        findViewById(R.id.content_panel).startAnimation(animation);

        poster_imageView.animate().setDuration(duration).
                scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta).
                setInterpolator(new DecelerateInterpolator()).
                withEndAction(endAction);

        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), currentStatusBarColor, statusBarColor);
        valueAnimator.setDuration(400);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                getWindow().setStatusBarColor((int) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    @Override
    public void onBackPressed() {
        runExitAnimation(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
