package com.nomtek.recyclerbucketlist.example

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.nomtek.recyclerbucketlist.example.delegates.ColorHomeListItemAdapterDelegate
import com.nomtek.recyclerbucketlist.example.delegates.CounterHomeListItemAdapterDelegate
import com.nomtek.recyclerbucketlist.example.delegates.FooterHomeListItemAdapterDelegate

class HomeListAdapter : ListDelegationAdapter<MutableList<HomeListItem>>() {

    init {
        delegatesManager.addDelegate(ColorHomeListItemAdapterDelegate())
        delegatesManager.addDelegate(CounterHomeListItemAdapterDelegate())
        delegatesManager.addDelegate(FooterHomeListItemAdapterDelegate())
    }
}


