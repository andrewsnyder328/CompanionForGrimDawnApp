package com.syntech.companionforgrimdawn

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Switch
import kotlinx.android.synthetic.main.view_filter.view.*


class FilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var filter = Filter()
        get() {
            return Filter(
                cb_ascendant.isChecked,
                cb_chaos.isChecked,
                cb_eldritch.isChecked,
                cb_order.isChecked,
                cb_primordial.isChecked,
                cb_starred.isChecked,
                switch_filter_mode.isChecked,
                switch_filter_type.isChecked
            )
        }

    init {
        if (attrs == null) {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        inflate(context, R.layout.view_filter, this)
    }

    fun setup(filter: Filter) {
        this.filter = filter
        cb_ascendant.isChecked = filter.ascendant
        cb_chaos.isChecked = filter.chaos
        cb_eldritch.isChecked = filter.eldritch
        cb_order.isChecked = filter.order
        cb_primordial.isChecked = filter.primordial
        cb_starred.isChecked = filter.starred
        switch_filter_mode.isChecked = filter.matchAny
        tv_filter_mode.setText(if (switch_filter_mode.isChecked) R.string.match_any else R.string.match_all)
        switch_filter_mode.setOnClickListener { v ->
            tv_filter_mode.setText(if ((v as Switch).isChecked) R.string.match_any else R.string.match_all)
        }
        switch_filter_type.isChecked = filter.matchRequirements
        tv_filter_type.setText(if (switch_filter_type.isChecked) R.string.match_requirements else R.string.match_rewards)
        switch_filter_type.setOnClickListener { v ->
            tv_filter_type.setText(if ((v as Switch).isChecked) R.string.match_requirements else R.string.match_rewards)
        }
    }
}