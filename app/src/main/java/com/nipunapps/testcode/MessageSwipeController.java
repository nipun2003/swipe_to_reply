package com.nipunapps.testcode;

import static android.view.Gravity.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class MessageSwipeController extends ItemTouchHelper.Callback {

    private Context context;
    private SwipeControllActions swipeControllActions;

    private Drawable imageDrawable, shareRound;
    private RecyclerView.ViewHolder currentItemViewHolder;
    private View mView;
    private float dX = 0f;
    private float replyButtonProgress = 0f;
    private Long lastReplyButtonAnimationTime = 0L;
    private boolean swipeBack = false, isVibrate = false, startTracking = false;

    public MessageSwipeController(Context context, SwipeControllActions swipeControllActions) {
        this.context = context;
        this.swipeControllActions = swipeControllActions;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        mView = viewHolder.itemView;
        imageDrawable = context.getDrawable(R.drawable.ic_reply_black_24dp);
        shareRound = context.getDrawable(R.drawable.ic_round_shape);
        return ItemTouchHelper.Callback.makeMovementFlags(ACTION_STATE_IDLE, RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder);
        }

        if (mView.getTranslationX() < convertTodp(130) || dX < this.dX) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            this.dX = dX;
            startTracking = true;
        }
        currentItemViewHolder = viewHolder;
        drawReplyButton(c);
    }

    private void drawReplyButton(Canvas c) {
        if (currentItemViewHolder == null) return;
        float translationX = mView.getTranslationX();
        Long newTime = System.currentTimeMillis();
        Long dt = Math.min(17L, newTime - lastReplyButtonAnimationTime);
        lastReplyButtonAnimationTime = newTime;
        boolean showing = translationX >= convertTodp(30);
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f;
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f;
                } else {
                    mView.invalidate();
                }
            }
        } else if (translationX <= 0.0f) {
            replyButtonProgress = 0f;
            startTracking = false;
            isVibrate = false;
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f;
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f;
                } else {
                    mView.invalidate();
                }
            }
        }
        int alpha;
        float scale;
        if (showing) {
            scale = (replyButtonProgress <= 0.8f) ?
                    1.2f * (replyButtonProgress / 0.8f)
                    :
                    1.2f - 0.2f * ((replyButtonProgress - 0.8f) / 0.2f);
            alpha = (int) Math.min(255f, 255 * (replyButtonProgress / 0.8f));
        } else {
            scale = replyButtonProgress;
            alpha = (int) Math.min(255f, 255 * replyButtonProgress);
        }
        shareRound.setAlpha(alpha);

        imageDrawable.setAlpha(alpha);
        if (startTracking) {
            if (!isVibrate && mView.getTranslationX() >= convertTodp(100)) {
                mView.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                );
                isVibrate = true;
            }
        }

        int x = (mView.getTranslationX() > convertTodp(130)) ?
                convertTodp(130) / 2
                :
                (int) (mView.getTranslationX() / 2);

        float y = (float) (mView.getTop() + mView.getMeasuredHeight() / 2);
        shareRound.setColorFilter(
                new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.purple_700), PorterDuff.Mode.MULTIPLY));

        shareRound.setBounds(
                (int) (x - convertTodp(18) * scale),
                (int) (y - convertTodp(18) * scale),
                (int) (x + convertTodp(18) * scale),
                (int) (y + convertTodp(18) * scale)
        );
        shareRound.draw(c);
        imageDrawable.setBounds(
                (int) (x - convertTodp(12) * scale),
                (int) (y - convertTodp(11) * scale),
                (int) (x + convertTodp(12) * scale),
                (int) (y + convertTodp(10) * scale)
        );
        imageDrawable.draw(c);
        shareRound.setAlpha(255);
        imageDrawable.setAlpha(255);
    }

    private void setTouchListener(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        recyclerView.setOnTouchListener((f, event) -> {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
            if (swipeBack) {
                if (Math.abs(mView.getTranslationX()) >= convertTodp(100)) {
                    swipeControllActions.showReplyUi(viewHolder.getAdapterPosition());
                }
            }
            return false;
        });
    }

    private int convertTodp(int pixel) {
        return AndroidUtils.INSTANCE.dp((float) pixel, context);
    }
}
