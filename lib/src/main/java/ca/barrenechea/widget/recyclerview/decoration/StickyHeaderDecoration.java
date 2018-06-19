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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * A sticky header decoration for android's RecyclerView.
 */
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
  public static final long NO_HEADER_ID = -1L;

  private Map<Long, RecyclerView.ViewHolder> headerCache;
  private StickyHeaderAdapter adapter;
  private boolean renderInline;
  private final Rect itemBounds = new Rect();

  private StickyHeaderAdapter.StickyHeaderPositionListener positionListener;

  /**
   * @param adapter
   *   the sticky header adapter to use
   */
  public StickyHeaderDecoration(@NonNull StickyHeaderAdapter adapter) {
    this(adapter, false);
  }

  /**
   * @param adapter
   *   the sticky header adapter to use
   */
  public StickyHeaderDecoration(@NonNull StickyHeaderAdapter adapter, boolean renderInline) {
    this.adapter = adapter;
    this.headerCache = new HashMap<>();
    this.renderInline = renderInline;
  }

  public void setPositionListener(StickyHeaderAdapter.StickyHeaderPositionListener positionListener) {
    this.positionListener = positionListener;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

    int position = parent.getChildAdapterPosition(view);
    int headerHeight = 0;

    if (position != RecyclerView.NO_POSITION && hasHeader(position) && showHeaderAboveItem(position)) {
      View header = getHeader(parent, position, true).itemView;
      headerHeight = getHeaderHeightForLayout(header);
    }

    outRect.set(0, headerHeight, 0, 0);
  }

  private boolean showHeaderAboveItem(int itemAdapterPosition) {
    if (itemAdapterPosition == 0) {
      return true;
    }
    return adapter.getHeaderId(itemAdapterPosition - 1) != adapter.getHeaderId(itemAdapterPosition);
  }

  /**
   * Clears the header view cache. Headers will be recreated and
   * rebound on list scroll after this method has been called.
   */
  public void clearHeaderCache() {
    headerCache.clear();
  }

  @Nullable
  public View findHeaderViewUnder(float x, float y) {
    for (RecyclerView.ViewHolder holder : headerCache.values()) {
      final View child = holder.itemView;
      final float translationX = child.getTranslationX();
      final float translationY = child.getTranslationY();

      if (x >= child.getLeft() + translationX &&
        x <= child.getRight() + translationX &&
        y >= child.getTop() + translationY &&
        y <= child.getBottom() + translationY) {
        return child;
      }
    }

    return null;
  }

  private boolean hasHeader(int position) {
    return adapter.getHeaderId(position) != NO_HEADER_ID;
  }

  @NonNull
  private RecyclerView.ViewHolder getHeader(@NonNull RecyclerView parent, int position, boolean shouldBind) {
    final long key = adapter.getHeaderId(position);

    if (headerCache.containsKey(key)) {
      final RecyclerView.ViewHolder holder = headerCache.get(key);
      if (shouldBind) {
        //noinspection unchecked
        adapter.onBindHeaderViewHolder(holder, position);
        final View header = holder.itemView;
        measureView(parent, header);
      }
      return holder;
    } else {
      final RecyclerView.ViewHolder holder = adapter.onCreateHeaderViewHolder(parent);
      final View header = holder.itemView;

      //noinspection unchecked
      adapter.onBindHeaderViewHolder(holder, position);
      measureView(parent, header);
      headerCache.put(key, holder);

      return holder;
    }
  }

  private void measureView(RecyclerView parent, View header) {
    int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
    int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

    int childWidth = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
    int childHeight = ViewGroup.getChildMeasureSpec(heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);

    header.measure(childWidth, childHeight);
    header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

    final int count = parent.getChildCount();
    long previousHeaderId = -1;

    for (int layoutPos = 0; layoutPos < count; layoutPos++) {
      final View child = parent.getChildAt(layoutPos);
      final int adapterPos = parent.getChildAdapterPosition(child);

      if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
        long headerId = adapter.getHeaderId(adapterPos);

        if (headerId != previousHeaderId) {
          itemBounds.set(0, 0, 0, 0);
          parent.getDecoratedBoundsWithMargins(child, itemBounds);

          previousHeaderId = headerId;
          View header = getHeader(parent, adapterPos, false).itemView;
          canvas.save();

          final int left = itemBounds.left;
          final int top = getHeaderTop(parent, itemBounds.top, adapterPos, layoutPos);
          canvas.translate(left, top);

          header.setTranslationX(left);
          header.setTranslationY(top);
          header.draw(canvas);
          canvas.restore();

          if (positionListener != null) {
            positionListener.onPositionChanged(headerId, left, top);
          }
        }
      }
    }
  }

  private int getHeaderTop(@NonNull RecyclerView parent, int top, int adapterPos, int layoutPos) {
    if (layoutPos == 0) {
      final int count = parent.getChildCount();
      final long currentId = adapter.getHeaderId(adapterPos);
      // find next view with header and compute the offscreen push if needed
      for (int i = 1; i < count; i++) {
        int adapterPosHere = parent.getChildAdapterPosition(parent.getChildAt(i));
        if (adapterPosHere != RecyclerView.NO_POSITION) {
          long nextId = adapter.getHeaderId(adapterPosHere);
          if (nextId != currentId) {
            final View next = parent.getChildAt(i);
            itemBounds.set(0, 0, 0, 0);
            parent.getDecoratedBoundsWithMargins(next, itemBounds);

            final int offset = ((int) next.getY()) - (itemBounds.top + getHeader(parent, adapterPosHere, false).itemView.getHeight());
            if (offset < 0) {
              return offset;
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

  private int getHeaderHeightForLayout(@NonNull View header) {
    return renderInline ? 0 : header.getHeight();
  }
}
