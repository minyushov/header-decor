/*
 * Copyright 2014 Eduardo Barrenechea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.barrenechea.widget.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * A double sticky header decoration for android's RecyclerView.
 */
public class DoubleHeaderDecoration extends RecyclerView.ItemDecoration {
    private DoubleHeaderAdapter adapter;
    private Map<Long, RecyclerView.ViewHolder> subHeaderCache;
    private Map<Long, RecyclerView.ViewHolder> headerCache;
    private boolean renderInline;
    private int betweenHeadersMargin = 0;
    private DoubleHeaderAdapter.DoubleHeaderPositionListener positionListener;

    /**
     * @param adapter the double header adapter to use
     */
    public DoubleHeaderDecoration(@NonNull DoubleHeaderAdapter adapter) {
        this(adapter, false);
    }

    /**
     * @param adapter the double header adapter to use
     */
    public DoubleHeaderDecoration(@NonNull DoubleHeaderAdapter adapter, boolean renderInline) {
        this.adapter = adapter;

        this.subHeaderCache = new HashMap<>();
        this.headerCache = new HashMap<>();
        this.renderInline = renderInline;
    }

    public void setPositionListener(DoubleHeaderAdapter.DoubleHeaderPositionListener positionListener) {
        this.positionListener = positionListener;
    }

    /**
     * Clears both the header and subheader view cache. Headers and subheaders will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearDoubleHeaderCache() {
        clearSubHeaderCache();
        clearHeaderCache();
    }

    /**
     * Clears the subheader view cache. Subheaders will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearSubHeaderCache() {
        subHeaderCache.clear();
    }

    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearHeaderCache() {
        headerCache.clear();
    }

    @NonNull
    private RecyclerView.ViewHolder getSubHeader(@NonNull RecyclerView parent, int position, boolean shouldBind) {
        final long key = adapter.getSubHeaderId(position);

        if (subHeaderCache.containsKey(key)) {
            RecyclerView.ViewHolder holder = subHeaderCache.get(key);
            if (shouldBind) {
                //noinspection unchecked
                adapter.onBindSubHeaderHolder(holder, position);
                final View header = holder.itemView;
                measureView(parent, header);
            }
            return holder;
        } else {
            final RecyclerView.ViewHolder holder = adapter.onCreateSubHeaderHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            adapter.onBindSubHeaderHolder(holder, position);
            measureView(parent, header);
            subHeaderCache.put(key, holder);

            return holder;
        }
    }

    @Nullable
    public View findHeaderViewUnder(float x, float y) {
        for (RecyclerView.ViewHolder holder : headerCache.values()) {
            final View child = holder.itemView;
            final float translationX = ViewCompat.getTranslationX(child);
            final float translationY = ViewCompat.getTranslationY(child);

            if (x >= child.getLeft() + translationX &&
                    x <= child.getRight() + translationX &&
                    y >= child.getTop() + translationY &&
                    y <= child.getBottom() + translationY) {
                return child;
            }
        }

        return null;
    }

    @Nullable
    public View findSubHeaderViewUnder(float x, float y) {
        for (RecyclerView.ViewHolder holder : subHeaderCache.values()) {
            final View child = holder.itemView;
            final float translationX = ViewCompat.getTranslationX(child);
            final float translationY = ViewCompat.getTranslationY(child);

            if (x >= child.getLeft() + translationX &&
                    x <= child.getRight() + translationX &&
                    y >= child.getTop() + translationY &&
                    y <= child.getBottom() + translationY) {
                return child;
            }
        }

        return null;
    }

    @NonNull
    private RecyclerView.ViewHolder getHeader(@NonNull RecyclerView parent, int position, boolean shouldBind) {
        final long key = adapter.getHeaderId(position);

        if (headerCache.containsKey(key)) {
            RecyclerView.ViewHolder holder = headerCache.get(key);
            if (shouldBind) {
                //noinspection unchecked
                adapter.onBindHeaderHolder(holder, position);
                final View header = holder.itemView;
                measureView(parent, header);
            }
            return holder;
        } else {
            final RecyclerView.ViewHolder holder = adapter.onCreateHeaderHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            adapter.onBindHeaderHolder(holder, position);
            measureView(parent, header);
            headerCache.put(key, holder);

            return holder;
        }
    }

    private void measureView(@NonNull RecyclerView parent, @NonNull View header) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);

        header.measure(childWidth, childHeight);
        header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
    }

    private boolean hasSubHeader(int position) {
        if (adapter.getSubHeaderId(position) == StickyHeaderDecoration.NO_HEADER_ID) {
            return false;
        }

        int previous = position - 1;
        return adapter.getSubHeaderId(position) != adapter.getSubHeaderId(previous);
    }

    private boolean hasHeader(int position) {
        if (position == 0) {
            return true;
        }

        int previous = position - 1;
        return adapter.getHeaderId(position) != adapter.getHeaderId(previous);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
            @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);

        int headerHeight = 0;

        if (position != RecyclerView.NO_POSITION) {
            if (hasHeader(position)) {
                View header = getHeader(parent, position, true).itemView;
                headerHeight += header.getHeight();
            }

            if (hasSubHeader(position))
            {
                View header = getSubHeader(parent, position, true).itemView;
                headerHeight += getSubHeaderHeightForLayout(header);
            }
        }

        outRect.set(0, headerHeight, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int count = parent.getChildCount();

        boolean headerDrawn = false;
        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            View child = parent.getChildAt(layoutPos);
            boolean visible = getAnimatedTop(child) > -child.getHeight()/* && child.getTop() < parent.getHeight()*/;
            final int adapterPos = parent.getChildAdapterPosition(child);
            if (visible && adapterPos != RecyclerView.NO_POSITION && (!headerDrawn || hasSubHeader(adapterPos) || hasHeader(adapterPos))) {
                int left, top;

                long headerId = adapter.getHeaderId(adapterPos);
                View header = getHeader(parent, adapterPos, false).itemView;

                long subHeaderId = adapter.getSubHeaderId(adapterPos);
                View subHeader = getSubHeader(parent, adapterPos, false).itemView;

                if (hasSubHeader(adapterPos))
                {
                    canvas.save();
                    left = child.getLeft();
                    top = getSubHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                  canvas.translate(left, top);
                    subHeader.setTranslationX(left);
                    subHeader.setTranslationY(top);
                    subHeader.draw(canvas);
                  canvas.restore();

                    if (positionListener != null) {
                        positionListener.onSubHeaderPositionChanged(subHeaderId, left, top);
                    }
                }
                // draw part of previous subheader which should be visible
                else if (adapterPos > 0 && adapter.getHeaderId(adapterPos) == adapter.getHeaderId(adapterPos - 1)) {
                    subHeader = getSubHeader(parent, adapterPos - 1, false).itemView;

                  canvas.save();
                    left = child.getLeft();
                    top = getSubHeaderTop(parent, child, header, subHeader, adapterPos - 1, layoutPos);
                  canvas.translate(left, top);
                    subHeader.setTranslationX(left);
                    subHeader.setTranslationY(top);
                    subHeader.draw(canvas);
                  canvas.restore();

                    if (positionListener != null) {
                        positionListener.onSubHeaderPositionChanged(subHeaderId, left, top);
                    }
                }

                if (!headerDrawn || hasHeader(adapterPos)) {
                    canvas.save();
                    left = child.getLeft();
                    top = getHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                    canvas.translate(left, top);
                    header.setTranslationX(left);
                    header.setTranslationY(top);
                    header.draw(canvas);
                  canvas.restore();

                    if (positionListener != null) {
                        positionListener.onHeaderPositionChanged(headerId, left, top);
                    }

                    // draw part of previous header which should be visible
                    if (getBetweenHeadersMargin() != 0 && top <= -getBetweenHeadersMargin() && adapterPos > 0 && adapter.getHeaderId(adapterPos) != adapter.getHeaderId(adapterPos - 1)) {
                        headerId = adapter.getHeaderId(adapterPos - 1);
                        header = getHeader(parent, adapterPos - 1, false).itemView;
                        child = parent.getChildAt(layoutPos);

                      canvas.save();
                        left = child.getLeft();
                        top = getHeaderTop(parent, child, header, subHeader, adapterPos - 1, layoutPos);
                        top += getBetweenHeadersMargin() * 2;
                      canvas.translate(left, top);
                        header.setTranslationX(left);
                        header.setTranslationY(top);
                        header.draw(canvas);
                      canvas.restore();

                        if (positionListener != null) {
                            positionListener.onHeaderPositionChanged(headerId, left, top);
                        }
                    }
                }

                headerDrawn = true;
            }
        }
    }

    private int getSubHeaderTop(@NonNull RecyclerView parent, @NonNull View child,
            @NonNull View header, @NonNull View subHeader, int adapterPos, int layoutPos) {

        int top = getAnimatedTop(child) - getSubHeaderHeightForLayout(subHeader);
        int maxTop = header.getHeight();
        //if (isFirstValidChild(layoutPos, parent))
        {
            final int count = parent.getChildCount();
            final long currentHeaderId = adapter.getHeaderId(adapterPos);
            final long currentSubHeaderId = adapter.getSubHeaderId(adapterPos);

            // find next view with sub-header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                final View next = parent.getChildAt(i);
                int adapterPosHere = parent.getChildAdapterPosition(next);
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    final long nextHeaderId = adapter.getHeaderId(adapterPosHere);
                    final long nextSubHeaderId = adapter.getSubHeaderId(adapterPosHere);

                    if ((nextSubHeaderId != currentSubHeaderId)) {
                        int headersHeight = getSubHeaderHeightForLayout(subHeader) + getSubHeader(parent, adapterPosHere, false).itemView.getHeight();
                        if (nextHeaderId != currentHeaderId) {
                            headersHeight += getHeader(parent, adapterPosHere, false).itemView.getHeight();
                        }

                        final int offset = getAnimatedTop(next) - headersHeight;
                        if (offset < header.getHeight()) {
                            return offset;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return Math.max(maxTop, top);
    }

    private int getHeaderTop(@NonNull RecyclerView parent, @NonNull View child,
            @NonNull View header, @NonNull View subHeader, int adapterPos, int layoutPos) {

        int top = getAnimatedTop(child) - header.getHeight() - getSubHeaderHeightForLayout(subHeader);
        if (isFirstValidChild(layoutPos, parent)) {
            final int count = parent.getChildCount();
            final long currentId = adapter.getHeaderId(adapterPos);

            // find next view with header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                View next = parent.getChildAt(i);
                int adapterPosHere = parent.getChildAdapterPosition(next);
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    long nextId = adapter.getHeaderId(adapterPosHere);
                    if (nextId != currentId) {
                        final int headersHeight = header.getHeight() + getHeader(parent, adapterPosHere, false).itemView.getHeight();
                        final int offset = getAnimatedTop(next) - headersHeight - getSubHeaderHeightForLayout(subHeader);

                        if (offset < getBetweenHeadersMargin()) {
                            return offset - getBetweenHeadersMargin();
                        } else {
                            break;
                        }
                    }
                }
            }

            top = Math.max(0, top);
        }

        return top;
    }

    /**
     * Space adjustment between 2 headers when the first over the second
     * @return space in pixels
     */
    protected int getBetweenHeadersMargin() {
        return betweenHeadersMargin;
    }

    public void setBetweenHeadersMargin(int betweenHeadersMargin) {
        this.betweenHeadersMargin = betweenHeadersMargin;
    }

    private boolean isFirstValidChild(int layoutPos, @NonNull RecyclerView parent) {
        boolean isFirstValidChild = true;
        for (int otherLayoutPos = layoutPos - 1; otherLayoutPos >= 0; --otherLayoutPos) {
            final View otherChild = parent.getChildAt(otherLayoutPos);
            if (parent.getChildAdapterPosition(otherChild) != RecyclerView.NO_POSITION) {
                boolean visible = getAnimatedTop(otherChild) > -otherChild.getHeight();
                if (visible) {
                    isFirstValidChild = false;
                    break;
                }
            }
        }
        return isFirstValidChild;
    }

    private int getAnimatedTop(@NonNull View child) {
        return child.getTop() + (int) child.getTranslationY();
    }

    private int getSubHeaderHeightForLayout(@NonNull View header) {
        return renderInline ? 0 : header.getHeight();
    }
}
