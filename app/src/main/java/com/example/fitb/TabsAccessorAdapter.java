package com.example.fitb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

//    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior) {
//        super(fm, behavior);
//    }

    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch(i){
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                ContactsFragment contactsFragment=new ContactsFragment();
                return contactsFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return  "Chats";
            case 1:
                return "Friend Requests";
            default:
                return null;

        }
    }
}
