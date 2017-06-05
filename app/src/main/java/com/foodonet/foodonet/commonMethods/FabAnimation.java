package com.foodonet.foodonet.commonMethods;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.view.animation.DecelerateInterpolator;

/**
 * animator for fab
 */
public class FabAnimation {
    /** static animator method for floating action button */
    public static void animateFAB(Context context, final FloatingActionButton fab, long duration, int imageResource, int color, boolean setVisible){
        // in order of denying user input during animation, turn the clickable to false
        fab.setClickable(false);
        if (!setVisible){
            // if there should be no fab visible, just hide it
            fab.hide();
        } else {
            // show the fab first, as it may have been hidden before
            fab.show();

            // animation
            final Bitmap imageBitmap = BitmapFactory.decodeResource(context.getResources(), imageResource);

            AnimatorSet animation = new AnimatorSet();

            ObjectAnimator colorAnimation = ObjectAnimator.ofInt(fab, "backgroundTint", fab.getBackgroundTintList().getDefaultColor(), color);
            colorAnimation.setEvaluator(new ArgbEvaluator());
            colorAnimation.setInterpolator(new DecelerateInterpolator());
            colorAnimation.setDuration(duration);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int animatedValue = (int) valueAnimator.getAnimatedValue();
                    fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                }
            });

            ObjectAnimator fadeOutImageAnimation = ObjectAnimator.ofInt(fab, "imageAlpha", 255, 0);
            fadeOutImageAnimation.setDuration(duration / 2);

            ObjectAnimator fadeInImageAnimation = ObjectAnimator.ofInt(fab, "imageAlpha", 0, 255);
            fadeInImageAnimation.setDuration(duration / 2);
            fadeInImageAnimation.setStartDelay(duration / 2);
            fadeInImageAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    // when this animation starts, it means the fab image is faded out, which means it's the time to change the image to the new one
                    fab.setImageBitmap(imageBitmap);
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                }
                @Override
                public void onAnimationCancel(Animator animator) {
                }
                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                    // when the animation set finishes, turn clickable back on
                    fab.setClickable(true);
                }
                @Override
                public void onAnimationCancel(Animator animator) {
                }
                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animation.play(colorAnimation)
                    .with(fadeOutImageAnimation)
                    .with(fadeInImageAnimation);
            animation.start();
        }
    }}
