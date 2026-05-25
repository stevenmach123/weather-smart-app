package edu.uiuc.cs427app.repositories.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.data.database.entities.ChatItem;
import edu.uiuc.cs427app.data.database.entities.User;

/**
 * Constructs a ChatAdapter.
 * populate dynamic  list of chat items to display in the RecyclerView
 * the callback listener triggered when a user selects an option
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    
    /**
     * Callback interface for handling option selection events in the chat adapter.
     */
    public interface OnOptionClickListener {
        /**
         * Called when a user selects an option in the chat.
         *
         * @param question the text of the selected option
         */
        void onOptionClick(String question);
    }

    private OnOptionClickListener onOptionClickListener;
    private List<ChatItem> chatItems;
    private final int TYPE_USER= 0;
    private final int TYPE_OTHER = 1;

    /**
     * Constructs a ChatAdapter.
     *
     * @param chatItems the list of chat items to display
     * @param listener  the listener for handling option click events
     */
    public ChatAdapter(List<ChatItem> chatItems, OnOptionClickListener listener) {
        this.chatItems = chatItems;
        this.onOptionClickListener = listener;
    }

    /**
     * Returns the view type of the item at the given position.
     *
     * @param position the position of the item in the list
     * @return TYPE_USER if the item is from the user,
     *         TYPE_OTHER if the item is from AI/other
     */
    @Override
    public int getItemViewType(int position) {
        ChatItem item = chatItems.get(position);
        return item.name.equals("ai") || item.name.isEmpty() ? TYPE_OTHER : TYPE_USER;
    }

    /**
     * Returns the total number of items in the adapter.
     *
     * @return the size of the chatItems list
     */
    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    /**
     * Creates a new ViewHolder based on the view type.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the type of the view (TYPE_USER or TYPE_OTHER)
     * @return a corresponding RecyclerView.ViewHolder instance
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view,parent);
        } else {
            View view = inflater.inflate(R.layout.item_chat_other, parent, false);
            return new OtherViewHolder(view,parent);
        }
    }

    /**
     * Binds data to the given ViewHolder.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);

        if(holder instanceof  UserViewHolder)
            ((UserViewHolder) holder).bind(item, position);
        else if (holder instanceof OtherViewHolder)
            ((OtherViewHolder) holder).bind(item,position);
    }

    /**
     * Sets the corner radius of a TextView background based on the message length.
     * Shorter messages get larger rounded corners, longer messages get smaller ones.
     * Uses mutate() to avoid affecting other views sharing the same drawable.
     *
     * @param tv   the TextView whose background will be modified
     * @param item the ChatItem containing the message used to determine radius
     */
    public void setBorderRadius(TextView tv, ChatItem item){
        float radius;
        if (item.message.length() < 30) {
            System.out.println("1: "+item.message);
            radius = 100f;
        } else if (item.message.length() < 60) {
            System.out.println("2: "+item.message);
            radius = 50f;
        } else {
            System.out.println("3: "+item.message);
            radius = 20f;
        }
        GradientDrawable drawable = (GradientDrawable) tv.getBackground().mutate();
        drawable.setCornerRadius(radius);
        tv.setBackground(drawable);
    }

    /**
     * ViewHolder for AI/other messages.
     *
     * This ViewHolder displays a simple text message (no options).
     */
    class OtherViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        /**
         * Creates a new OtherViewHolder.
         *
         * @param itemView the root view of the item layout
         * @param parent   the parent ViewGroup (RecyclerView)
         */
        public OtherViewHolder(View itemView,ViewGroup parent) {
            super(itemView);
            messageTextView= itemView.findViewById(R.id.message_text);
        }

        /**
         * Binds a ChatItem to the view.
         *
         * @param item     the ChatItem containing the message
         * @param position the position of the item in the list
         */
        public void bind(ChatItem item, int position){
            messageTextView.setText(item.message);
            setBorderRadius(messageTextView,item);
        }
    }

    /**
     * ViewHolder for user messages with selectable options.
     *
     * This ViewHolder dynamically creates option buttons (TextViews),
     * handles selection behavior, and triggers callbacks when an option is clicked.
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout optionsContainer;
        private List<TextView> optionTextViews;
        private View itemView;
        private ViewGroup vg;

        /**
         * Creates a new UserViewHolder.
         *
         * @param itemView the root view of the item layout
         * @param parent   the parent ViewGroup (RecyclerView)
         */
        public UserViewHolder(View itemView,ViewGroup parent) {
            super(itemView);
            this.vg = parent; this.itemView =itemView;
            optionsContainer = itemView.findViewById(R.id.options_container);
            optionTextViews = new ArrayList<>();
        }

        /**
         * Hides (disables) an option view.
         *
         * @param v the TextView representing the option
         */
        public void disableOption(TextView v){
            v.setVisibility(View.GONE);
        }

        /**
         * Highlights and enables the selected option.
         * This changes background color, text color, and keeps the view visible.
         * @param v the selected TextView option
         */
        public void enableOption(TextView v,ChatItem item){
            v.setVisibility(View.VISIBLE);
            GradientDrawable drawable = (GradientDrawable) v.getBackground().mutate();
            int blue = ContextCompat.getColor(vg.getContext(), R.color.blue);
            drawable.setStroke(0,Color.BLUE);
            drawable.setColor(ColorStateList.valueOf(blue));
            v.setBackground(drawable);
            //v.setBackgroundTintList(ColorStateList.valueOf(blue));
            v.setTextColor(Color.WHITE);

        }

        /**
         * Converts density-independent pixels (dp) to pixels (px).
         *
         * @param sizeInDp the size in dp
         * @return the converted size in pixels
         */
        public  int DPtoPX(double sizeInDp){
            double scale = vg.getContext().getResources().getDisplayMetrics().density* 1.65;
            return  (int) (sizeInDp * scale);
        }

        /**
         * Binds a ChatItem by dynamically creating option views.
         *Clears previous views
         * Creates TextViews for each option
         * Applies styling and padding
         * Handles click: disables others, highlights selected, triggers callback
         * @param item     the ChatItem containing option data
         * @param position the position of the item in the list
         */
        public void bind(ChatItem item, int position){
            optionsContainer.removeAllViews();
            optionTextViews.clear();
            for(int i =0; i<  item.options.size();i++){
                TextView optionView = new TextView(vg.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                optionView.setLayoutParams(params);
                optionView.setText(item.options.get(i));
                optionView.setTextColor(ContextCompat.getColor(vg.getContext(), R.color.black));
                optionView.setBackground( ContextCompat.getDrawable(vg.getContext(),R.drawable.text_border));
                optionView.setPadding(DPtoPX(10),DPtoPX(6),DPtoPX(10),DPtoPX(6));
                setBorderRadius(optionView,item);
                optionView.setOnClickListener(v -> {
                     for (TextView option : optionTextViews) {
                         disableOption(option);
                     }
                     enableOption(optionView,item);
                    if (onOptionClickListener != null) {
                        onOptionClickListener.onOptionClick(optionView.getText().toString());
                    }
                });

                optionsContainer.addView(optionView);
                optionTextViews.add(optionView);

            }
        }
    }
}
