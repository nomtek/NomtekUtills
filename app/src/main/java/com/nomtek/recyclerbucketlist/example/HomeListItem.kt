package com.nomtek.recyclerbucketlist.example

interface HomeListItem

data class CounterHomeListItem(val counterValue: Int) : HomeListItem

data class ColorHomeListItem(val color: Int) : HomeListItem

data class FooterHomeListItem(val onAddColorButtonClicked: () -> Unit,
                              val onRemoveColorButtonClicked: () -> Unit) : HomeListItem