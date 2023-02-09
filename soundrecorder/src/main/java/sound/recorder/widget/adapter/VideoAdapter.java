package sound.recorder.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import sound.recorder.widget.R;
import sound.recorder.widget.model.Video;
import sound.recorder.widget.util.HFRecyclerViewAdapter;

public class VideoAdapter extends HFRecyclerViewAdapter<Video,VideoAdapter.ItemViewHolder> {

    private OnItemClickListener mListener;


    public VideoAdapter(Context context) {
        super(context);
        this.mContext = context;


    }

    @Override
    public void footerOnVisibleItem() {

    }


    @Override
    public ItemViewHolder onCreateDataItemViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);

        return new ItemViewHolder(v);
    }

    @Override
    public void onBindDataItemViewHolder(ItemViewHolder holder, final int position) {

        Video video = getData().get(position);
        holder.tvTitle.setText(video.getTitle());
        holder.tvDescription.setText(video.getDescription());
        holder.tvUrl.setText(video.getUrl());
        holder.tvUrl.setVisibility(View.GONE);
        holder.tvDatepublish.setText(video.getDatepublish());

        Picasso.get()
                .load((video.getThumbnail().equals("")) ? null : video.getThumbnail())
                .into(holder.ivThumbnail);

        holder.cvVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v,position);
            }
        });


    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private ImageView ivThumbnail;
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvUrl;
        private TextView tvDatepublish;
        private CardView cvVideo;


        public ItemViewHolder(View convertView) {
            super(convertView);


            ivThumbnail   = convertView.findViewById(R.id.iv_thuumnail);
            tvTitle       = convertView.findViewById(R.id.tv_title);
            tvDescription = convertView.findViewById(R.id.tv_description);
            tvUrl         = convertView.findViewById(R.id.tv_url);
            tvDatepublish = convertView.findViewById(R.id.tv_datepublish);
            cvVideo       = convertView.findViewById(R.id.cv_video);

            convertView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }
        }

    }

    public void setClickListener(OnItemClickListener clickListener) {
        this.mListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
