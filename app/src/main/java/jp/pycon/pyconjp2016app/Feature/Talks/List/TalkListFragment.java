package jp.pycon.pyconjp2016app.Feature.Talks.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import jp.pycon.pyconjp2016app.API.Client.APIClient;
import jp.pycon.pyconjp2016app.App;
import jp.pycon.pyconjp2016app.Feature.Talks.Adapter.RealmScheduleAdapter;
import jp.pycon.pyconjp2016app.Feature.Talks.Detail.TalkDetailActivity;
import jp.pycon.pyconjp2016app.Model.PyConJP.PresentationDetailEntity;
import jp.pycon.pyconjp2016app.Model.Realm.RealmPresentationObject;
import jp.pycon.pyconjp2016app.R;
import jp.pycon.pyconjp2016app.Util.RealmUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by rhoboro on 4/23/16.
 */
public class TalkListFragment extends Fragment {

    private Context mContext;
    private Realm realm;
    private RecyclerView recyclerView;
    private RealmResults<RealmPresentationObject> schedules;
    private RealmChangeListener realmListener;
    RealmScheduleAdapter adapter;

    public TalkListFragment() {

    }

    public static TalkListFragment newInstance(int position, boolean bookmark) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putBoolean("bookmark", bookmark);
        TalkListFragment fragment = new TalkListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_talk_list, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.talk_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.argb(15, 0, 0, 0));
        final int dividerHeight = (int) (1 * getResources().getDisplayMetrics().density);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);

                final RecyclerView.LayoutManager manager = parent.getLayoutManager();

                final int left = parent.getPaddingLeft();
                final int right = parent.getWidth() - parent.getPaddingRight();

                final int childCount = parent.getChildCount();
                for (int i = 1; i < childCount; i++) {
                    final View child = parent.getChildAt(i);
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    if (params.getViewLayoutPosition() == 0) {
                        continue;
                    }
                    final int top = manager.getDecoratedTop(child) - params.topMargin + Math.round(ViewCompat.getTranslationY(child));
                    final int bottom = top + dividerHeight;
                    c.drawRect(left, top, right, bottom, paint);
                }
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
                int top = position == 0 ? 0 : dividerHeight;
                outRect.set(0, top, 0, 0);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        realm = Realm.getDefaultInstance();
        setupRecycleView();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        schedules.removeChangeListener(realmListener);
        realm.close();
    }

    private void setupRecycleView() {
        Bundle bundle = getArguments();
        final boolean bookmark = bundle.getBoolean("bookmark", false);
        final int position = bundle.getInt("position");
        schedules = bookmark ? RealmUtil.getBookmarkTalks(mContext, realm, position) : RealmUtil.getAllTalks(realm, position);
        adapter = new RealmScheduleAdapter(getContext(), schedules);
        adapter.setOnClickListener(new RealmScheduleAdapter.RealmScheduleAdapterListener() {
            @Override
            public void onClick(int pk) {
                TalkDetailActivity.start(mContext, pk);
            }
        });
        recyclerView.setAdapter(adapter);
        realmListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {
                adapter.notifyDataSetChanged();
            }
        };
        schedules.addChangeListener(realmListener);
    }
}
