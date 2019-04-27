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

package ca.barrenechea.stickyheaders.ui;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import ca.barrenechea.stickyheaders.R;
import ca.barrenechea.stickyheaders.widget.DoubleHeaderTestAdapter;
import ca.barrenechea.widget.recyclerview.decoration.DoubleHeaderAdapter;
import ca.barrenechea.widget.recyclerview.decoration.DoubleHeaderDecoration;

public class DoubleHeaderFragment extends BaseDecorationFragment implements RecyclerView.OnItemTouchListener {
  private DoubleHeaderDecoration decor;

  @Override
  protected void setAdapterAndDecor(RecyclerView list) {
    final DoubleHeaderTestAdapter adapter = new DoubleHeaderTestAdapter(this.getActivity());
    decor = new DoubleHeaderDecoration(adapter);
    decor.setPositionListener(new DoubleHeaderAdapter.DoubleHeaderPositionListener() {
      @Override
      public void onHeaderPositionChanged(long headerId, int x, int y) {
        Log.d("DoubleHeader", String.format(Locale.US, "Header %s: x = %s, y = %s", headerId, x, y));
      }

      @Override
      public void onSubHeaderPositionChanged(long subHeaderId, int x, int y) {
        Log.d("DoubleHeader", String.format(Locale.US, "SubHeader %s: x = %s, y = %s", subHeaderId, x, y));
      }
    });

    setHasOptionsMenu(true);

    list.setAdapter(adapter);
    list.addItemDecoration(decor, 1);
    list.addOnItemTouchListener(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_clear_cache) {
      decor.clearDoubleHeaderCache();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
    // really bad click detection just for demonstration purposes
    // it will not allow the list to scroll if the swipe motion starts
    // on top of a header
    return rv.findChildViewUnder(e.getX(), e.getY()) == null;
  }

  @Override
  public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    // only use the "UP" motion event, discard all others
    if (e.getAction() != MotionEvent.ACTION_UP) {
      return;
    }

    // find the header that was clicked
    View view = decor.findHeaderViewUnder(e.getX(), e.getY());

    if (view == null) {
      // or the subheader, if the header is null
      view = decor.findSubHeaderViewUnder(e.getX(), e.getY());
    }

    if (view instanceof TextView) {
      Toast.makeText(this.getActivity(), ((TextView) view).getText() + " clicked", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    // do nothing
  }
}