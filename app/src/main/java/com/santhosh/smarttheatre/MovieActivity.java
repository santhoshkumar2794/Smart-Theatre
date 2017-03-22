package com.santhosh.smarttheatre;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.santhosh.smarttheatre.database.FavouriteDataSet;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santhosh-3366 on 07/02/17.
 */

public class MovieActivity extends AppCompatActivity {

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

    @BindView(R.id.title_view)
    TextView titleView;
    @BindView(R.id.overview)
    TextView overViewContainer;
    @BindView(R.id.release_date)
    TextView release_date;
    @BindView(R.id.rating_view)
    TextView rating_view;

    @BindView(R.id.backdrop_image)
    ImageView backdrop_imageView;
    @BindView(R.id.holder_image)
    ImageView poster_imageView;

    private MovieData movieData;
    private FavouriteDataSet favouriteDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_activity);

        ButterKnife.bind(this);
        statusBarColor = getWindow().getStatusBarColor();

        favouriteDataSet = new FavouriteDataSet(this);
        favouriteDataSet.openDB();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("extra");
        thumbnailTop = bundle.getInt("PROPNAME_SCREENLOCATION_TOP");
        thumbnailLeft = bundle.getInt("PROPNAME_SCREENLOCATION_LEFT");
        thumbnailWidth = bundle.getInt("PROPNAME_WIDTH");
        thumbnailHeight = bundle.getInt("PROPNAME_HEIGHT");

        movieData = bundle.getParcelable("movieData");

        final FABToggle floatingActionButton = (FABToggle) findViewById(R.id.floatingActionButton);
        floatingActionButton.setChecked(favouriteDataSet.hasItem(movieData.id));
        favouriteDataSet.getFavList();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("clicked","here "+floatingActionButton.isChecked());
                if (movieData!=null) {
                 movieData.favourites = !floatingActionButton.isChecked();
                    favouriteDataSet.insertItem(movieData);
                }
                floatingActionButton.toggle();
            }
        });

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
                            if (isLight(currentStatusBarColor)) {
                                setLightStatusBar(backdrop_imageView);
                            }
                            ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), statusBarColor, currentStatusBarColor);
                            valueAnimator.setDuration(400);
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getWindow().setStatusBarColor((int) valueAnimator.getAnimatedValue());
                                    }
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

        titleView.setText(movieData.original_title);
        overViewContainer.setText(movieData.overview);
        release_date.setText(movieData.release_date);
        rating_view.setText(String.valueOf(movieData.vote_average));
        ((RatingBar) findViewById(R.id.rating_bar)).setRating(movieData.vote_average / 2);

        mBackground = new ColorDrawable(Color.WHITE);
        findViewById(R.id.top_level_view).setBackground(mBackground);
        if (savedInstanceState == null) {
            activityTransition(poster_imageView);
        }
        String trailer_URL = "https://api.themoviedb.org/3/movie/"+ movieData.id+"/videos?api_key="+MainActivity.API_KEY+"&language=en-US";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, trailer_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    trailerJSON(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        String review_URL = "https://api.themoviedb.org/3/movie/"+ movieData.id+"/reviews?api_key="+MainActivity.API_KEY+"&language=en-US";

        JsonObjectRequest reviewRequest = new JsonObjectRequest(Request.Method.GET, review_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    reviewJSON(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
        Log.e("movie","ID  "+ movieData.id);
        queue.add(reviewRequest);
    }

    private void trailerJSON(JSONObject response) throws Exception{
        JSONArray jsonArray = response.getJSONArray("results");
        LinearLayout trialer_container = (LinearLayout) findViewById(R.id.trailer_container);
        int visibilty = jsonArray.length()>0 ? View.VISIBLE : View.INVISIBLE;
        trialer_container.setVisibility(visibilty);
        for (int i=0;i<jsonArray.length();i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String imageURL = "https://img.youtube.com/vi/" + jsonObject.get("key") + "/sddefault.jpg";
            final ImageView imageView = new ImageView(this);
            imageView.setTag(jsonObject.get("key"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400,300);
            params.leftMargin = 30;
            params.rightMargin =30;
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            trialer_container.addView(imageView);
            Picasso.with(this).load(imageURL).placeholder(R.drawable.image_placeholder).into(imageView);
            Log.e("image", "url " + imageURL);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v="+imageView.getTag()));
                    startActivity(intent);
                }
            });
        }
    }

    private void reviewJSON(JSONObject response) throws Exception{
        JSONArray jsonArray = response.getJSONArray("results");
        LinearLayout review_container = (LinearLayout) findViewById(R.id.review_container);
        int visibilty = jsonArray.length()>0 ? View.VISIBLE : View.INVISIBLE;
        review_container.setVisibility(visibilty);
        for (int i=0;i<jsonArray.length();i++) {
            LinearLayout reviewBox = new LinearLayout(this);
            reviewBox.setOrientation(LinearLayout.VERTICAL);

            JSONObject jsonObject = jsonArray.getJSONObject(i);
            SpannableStringBuilder author = new SpannableStringBuilder(jsonObject.getString("author"));
            int authorLength  = author.length();
            author.setSpan(new AbsoluteSizeSpan(45),0,author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            author.setSpan(Typeface.BOLD,0,authorLength,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            author.setSpan(new ForegroundColorSpan(Color.BLACK),0,authorLength,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            author.append("\n");
            author.append(jsonObject.getString("content"));
            author.setSpan(new AbsoluteSizeSpan(35),authorLength,author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            final TextView textView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = (int)(5*MainActivity.deviceDensity);
            params.leftMargin = (int)(5*MainActivity.deviceDensity);
            textView.setLayoutParams(params);
            textView.setText(author);
            textView.setMaxLines(4);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            reviewBox.addView(textView);

            final TextView readMore = new TextView(this);
            LinearLayout.LayoutParams readMoreParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            readMoreParams.gravity = Gravity.END;
            readMoreParams.rightMargin = (int)(10*MainActivity.deviceDensity);
            readMoreParams.topMargin = (int)(5*MainActivity.deviceDensity);
            readMore.setLayoutParams(readMoreParams);
            readMore.setTag(i);
            readMore.setText("Read More...");
            readMore.setTextColor(Color.BLACK);
            readMore.setTextSize(14);
            readMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout view1 = (LinearLayout)((LinearLayout) findViewById(R.id.review_container)).getChildAt((int)view.getTag()+1);
                    TextView review = (TextView) view1.getChildAt(0);
                    if (review.getMaxLines()!=Integer.MAX_VALUE) {
                        review.setMaxLines(Integer.MAX_VALUE);
                        review.setEllipsize(null);
                        readMore.setText("Read Less...");
                    }else {
                        review.setMaxLines(4);
                        review.setEllipsize(TextUtils.TruncateAt.END);
                        readMore.setText("Read More...");
                    }
                }
            });
            reviewBox.addView(readMore);

            View view = new View(this);
            LinearLayout.LayoutParams viewParam = new LinearLayout.LayoutParams((int)(MainActivity.screenWidth/1.5f),(int)(1*MainActivity.deviceDensity));
            viewParam.topMargin = (int)(10*MainActivity.deviceDensity);
            viewParam.gravity = Gravity.CENTER;
            view.setLayoutParams(viewParam);
            view.setBackgroundColor(Color.BLACK);
            reviewBox.addView(view);

            review_container.addView(reviewBox);
        }
    }

    private void activityTransition(final ImageView poster_imageView) {
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
        findViewById(R.id.floatingActionButton).startAnimation(animation);

        poster_imageView.animate().setDuration(duration).
                scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta).
                setInterpolator(new DecelerateInterpolator()).
                withEndAction(endAction);

        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), currentStatusBarColor, statusBarColor);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor((int) valueAnimator.getAnimatedValue());
                }
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
        favouriteDataSet.closeDB();
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        favouriteDataSet.openDB();
        super.onResume();
    }
}
