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

package ca.barrenechea.stickyheaders.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import ca.barrenechea.stickyheaders.R;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;

public class InlineStickyTestAdapter extends RecyclerView.Adapter<InlineStickyTestAdapter.ViewHolder> implements StickyHeaderAdapter<InlineStickyTestAdapter.HeaderHolder> {
  private final LayoutInflater inflater;

  public InlineStickyTestAdapter(Context context) {
    inflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_inline_test, viewGroup, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
    viewHolder.item.setText(String.format(Locale.US, "Item %s", position));
  }

  @Override
  public int getItemCount() {
    return 30;
  }

  @Override
  public long getHeaderId(int position) {
    return (long) position % 2;
  }

  @NonNull
  @Override
  public HeaderHolder onCreateHeaderViewHolder(@NonNull ViewGroup parent) {
    return new HeaderHolder(inflater.inflate(R.layout.header_inline_test, parent, false));
  }

  @Override
  public void onBindHeaderViewHolder(@NonNull HeaderHolder viewHolder, int position) {
    viewHolder.header.setText(String.valueOf(getHeaderId(position)));
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView item;

    ViewHolder(View itemView) {
      super(itemView);

      item = (TextView) itemView;
    }
  }

  static class HeaderHolder extends RecyclerView.ViewHolder {
    TextView header;

    HeaderHolder(View itemView) {
      super(itemView);

      header = (TextView) itemView;
    }
  }
}
