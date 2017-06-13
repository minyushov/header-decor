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
    private DoubleHeaderAdapter mAdapter;
    private Map<Long, RecyclerView.ViewHolder> mSubHeaderCache;
    private Map<Long, RecyclerView.ViewHolder> mHeaderCache;
    private boolean mRenderInline;
    private int betweenHeadersMargin = 0;
    private DoubleHeaderAdapter.DoubleHeaderPositionListener positionListener;

    /**
     * @param adapter the double header adapter to use
     */
    public DoubleHeaderDecoration(DoubleHeaderAdapter adapter) {
        this(adapter, false);
    }

    /**
     * @param adapter the double header adapter to use
     */
    public DoubleHeaderDecoration(DoubleHeaderAdapter adapter, boolean renderInline) {
        mAdapter = adapter;

        mSubHeaderCache = new HashMap<>();
        mHeaderCache = new HashMap<>();
        mRenderInline = renderInline;
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
        mSubHeaderCache.clear();
    }

    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearHeaderCache() {
        mHeaderCache.clear();
    }

    private RecyclerView.ViewHolder getSubHeader(RecyclerView parent, int position, boolean shouldBind) {
        final long key = mAdapter.getSubHeaderId(position);

        if (mSubHeaderCache.containsKey(key)) {
            RecyclerView.ViewHolder holder = mSubHeaderCache.get(key);
            if (shouldBind) {
                //noinspection unchecked
                mAdapter.onBindSubHeaderHolder(holder, position);
            }
            return holder;
        } else {
            final RecyclerView.ViewHolder holder = mAdapter.onCreateSubHeaderHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            mAdapter.onBindSubHeaderHolder(holder, position);
            measureView(parent, header);
            mSubHeaderCache.put(key, holder);

            return holder;
        }
    }

    public View findHeaderViewUnder(float x, float y) {
        for (RecyclerView.ViewHolder holder : mHeaderCache.values()) {
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

    public View findSubHeaderViewUnder(float x, float y) {
        for (RecyclerView.ViewHolder holder : mSubHeaderCache.values()) {
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

    private RecyclerView.ViewHolder getHeader(RecyclerView parent, int position, boolean shouldBind) {
        final long key = mAdapter.getHeaderId(position);

        if (mHeaderCache.containsKey(key)) {
            RecyclerView.ViewHolder holder =  mHeaderCache.get(key);
            if (shouldBind) {
                //noinspection unchecked
                mAdapter.onBindHeaderHolder(holder, position);
            }
            return holder;
        } else {
            final RecyclerView.ViewHolder holder = mAdapter.onCreateHeaderHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            mAdapter.onBindHeaderHolder(holder, position);
            measureView(parent, header);
            mHeaderCache.put(key, holder);

            return holder;
        }
    }

    private void measureView(RecyclerView parent, View header) {
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
        if (mAdapter.getSubHeaderId(position) == StickyHeaderDecoration.NO_HEADER_ID) {
            return false;
        }

        int previous = position - 1;
        return mAdapter.getSubHeaderId(position) != mAdapter.getSubHeaderId(previous);
    }

    private boolean hasHeader(int position) {
        if (position == 0) {
            return true;
        }

        int previous = position - 1;
        return mAdapter.getHeaderId(position) != mAdapter.getHeaderId(previous);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
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
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int count = parent.getChildCount();

        boolean headerDrawn = false;
        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            View child = parent.getChildAt(layoutPos);
            boolean visible = getAnimatedTop(child) > -child.getHeight()/* && child.getTop() < parent.getHeight()*/;
            final int adapterPos = parent.getChildAdapterPosition(child);
            if (visible && adapterPos != RecyclerView.NO_POSITION && (!headerDrawn || hasSubHeader(adapterPos) || hasHeader(adapterPos))) {
                int left, top;

                long headerId = mAdapter.getHeaderId(adapterPos);
                View header = getHeader(parent, adapterPos, false).itemView;

                long subHeaderId = mAdapter.getSubHeaderId(adapterPos);
                View subHeader = getSubHeader(parent, adapterPos, false).itemView;

                if (hasSubHeader(adapterPos))
                {
                    c.save();
                    left = child.getLeft();
                    top = getSubHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                    c.translate(left, top);
                    subHeader.setTranslationX(left);
                    subHeader.setTranslationY(top);
                    subHeader.draw(c);
                    c.restore();

                    if (positionListener != null) {
                        positionListener.onSubHeaderPositionChanged(subHeaderId, left, top);
                    }
                }
                // draw part of previous subheader which should be visible
                else if (adapterPos > 0 && mAdapter.getHeaderId(adapterPos) == mAdapter.getHeaderId(adapterPos - 1)) {
                    subHeader = getSubHeader(parent, adapterPos - 1, false).itemView;

                    c.save();
                    left = child.getLeft();
                    top = getSubHeaderTop(parent, child, header, subHeader, adapterPos - 1, layoutPos);
                    c.translate(left, top);
                    subHeader.setTranslationX(left);
                    subHeader.setTranslationY(top);
                    subHeader.draw(c);
                    c.restore();

                    if (positionListener != null) {
                        positionListener.onSubHeaderPositionChanged(subHeaderId, left, top);
                    }
                }

                if (!headerDrawn || hasHeader(adapterPos)) {
                    c.save();
                    left = child.getLeft();
                    top = getHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                    c.translate(left, top);
                    header.setTranslationX(left);
                    header.setTranslationY(top);
                    header.draw(c);
                    c.restore();

                    if (positionListener != null) {
                        positionListener.onHeaderPositionChanged(headerId, left, top);
                    }

                    // draw part of previous header which should be visible
                    if (getBetweenHeadersMargin() != 0 && top <= -getBetweenHeadersMargin() && adapterPos > 0 && mAdapter.getHeaderId(adapterPos) != mAdapter.getHeaderId(adapterPos - 1)) {
                        headerId = mAdapter.getHeaderId(adapterPos - 1);
                        header = getHeader(parent, adapterPos - 1, false).itemView;
                        child = parent.getChildAt(layoutPos);

                        c.save();
                        left = child.getLeft();
                        top = getHeaderTop(parent, child, header, subHeader, adapterPos - 1, layoutPos);
                        top += getBetweenHeadersMargin() * 2;
                        c.translate(left, top);
                        header.setTranslationX(left);
                        header.setTranslationY(top);
                        header.draw(c);
                        c.restore();

                        if (positionListener != null) {
                            positionListener.onHeaderPositionChanged(headerId, left, top);
                        }
                    }
                }

                headerDrawn = true;
            }
        }
    }

    private int getSubHeaderTop(RecyclerView parent, View child, View header, View subHeader, int adapterPos, int layoutPos) {
        int top = getAnimatedTop(child) - getSubHeaderHeightForLayout(subHeader);
        int maxTop = header.getHeight();
        //if (isFirstValidChild(layoutPos, parent))
        {
            final int count = parent.getChildCount();
            final long currentHeaderId = mAdapter.getHeaderId(adapterPos);
            final long currentSubHeaderId = mAdapter.getSubHeaderId(adapterPos);

            // find next view with sub-header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                final View next = parent.getChildAt(i);
                int adapterPosHere = parent.getChildAdapterPosition(next);
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    final long nextHeaderId = mAdapter.getHeaderId(adapterPosHere);
                    final long nextSubHeaderId = mAdapter.getSubHeaderId(adapterPosHere);

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

    private int getHeaderTop(RecyclerView parent, View child, View header, View subHeader, int adapterPos, int layoutPos) {
        int top = getAnimatedTop(child) - header.getHeight() - getSubHeaderHeightForLayout(subHeader);
        if (isFirstValidChild(layoutPos, parent)) {
            final int count = parent.getChildCount();
            final long currentId = mAdapter.getHeaderId(adapterPos);

            // find next view with header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                View next = parent.getChildAt(i);
                int adapterPosHere = parent.getChildAdapterPosition(next);
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    long nextId = mAdapter.getHeaderId(adapterPosHere);
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

    private boolean isFirstValidChild(int layoutPos, RecyclerView parent) {
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

    private int getAnimatedTop(View child) {
        return child.getTop() + (int)child.getTranslationY();
    }

    private int getSubHeaderHeightForLayout(View header) {
        return mRenderInline ? 0 : header.getHeight();
    }
}
