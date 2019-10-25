package com.syntech.companionforgrimdawn

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_constellation.view.*


class ConstellationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        if (attrs == null) {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        inflate(context, R.layout.view_constellation, this)
    }

    fun setup(
        item: Constellation,
        resources: Resource,
        listener: IConstellationView,
        enforceRules: Boolean
    ) {
        tv_name.text = item.name
        tv_points.text = "Points: ${item.points}"
        setupResources(item)
        setupButton(item, resources, listener, enforceRules)
        setupStar(item, listener)
        iv_constellation.setImageDrawable(getDrawable(item.imageSrc))
    }

    private fun setupResources(item: Constellation) {
        //requirements
        if (item.requirements.ascendant > 0) {
            iv_ascendant_req.visibility = View.VISIBLE
            tv_ascendant_req.visibility = View.VISIBLE
            tv_ascendant_req.text = item.requirements.ascendant.toString()
        } else {
            iv_ascendant_req.visibility = View.GONE
            tv_ascendant_req.visibility = View.GONE
        }
        if (item.requirements.chaos > 0) {
            iv_chaos_req.visibility = View.VISIBLE
            tv_chaos_req.visibility = View.VISIBLE
            tv_chaos_req.text = item.requirements.chaos.toString()
        } else {
            iv_chaos_req.visibility = View.GONE
            tv_chaos_req.visibility = View.GONE
        }
        if (item.requirements.eldritch > 0) {
            iv_eldritch_req.visibility = View.VISIBLE
            tv_eldritch_req.visibility = View.VISIBLE
            tv_eldritch_req.text = item.requirements.eldritch.toString()
        } else {
            iv_eldritch_req.visibility = View.GONE
            tv_eldritch_req.visibility = View.GONE
        }
        if (item.requirements.order > 0) {
            iv_order_req.visibility = View.VISIBLE
            tv_order_req.visibility = View.VISIBLE
            tv_order_req.text = item.requirements.order.toString()
        } else {
            iv_order_req.visibility = View.GONE
            tv_order_req.visibility = View.GONE
        }
        if (item.requirements.primordial > 0) {
            iv_primordial_req.visibility = View.VISIBLE
            tv_primordial_req.visibility = View.VISIBLE
            tv_primordial_req.text = item.requirements.primordial.toString()
        } else {
            iv_primordial_req.visibility = View.GONE
            tv_primordial_req.visibility = View.GONE
        }
        if (item.requirements.ascendant == 0
            && item.requirements.chaos == 0
            && item.requirements.eldritch == 0
            && item.requirements.order == 0
            && item.requirements.primordial == 0
        ) {
            vg_required.visibility = View.GONE
            tv_required.visibility = View.GONE
        } else {
            vg_required.visibility = View.VISIBLE
            tv_required.visibility = View.VISIBLE
        }

        //rewards
        if (item.rewards.ascendant > 0) {
            iv_ascendant_reward.visibility = View.VISIBLE
            tv_ascendant_reward.visibility = View.VISIBLE
            tv_ascendant_reward.text = item.rewards.ascendant.toString()
        } else {
            iv_ascendant_reward.visibility = View.GONE
            tv_ascendant_reward.visibility = View.GONE
        }
        if (item.rewards.chaos > 0) {
            iv_chaos_reward.visibility = View.VISIBLE
            tv_chaos_reward.visibility = View.VISIBLE
            tv_chaos_reward.text = item.rewards.chaos.toString()
        } else {
            iv_chaos_reward.visibility = View.GONE
            tv_chaos_reward.visibility = View.GONE
        }
        if (item.rewards.eldritch > 0) {
            iv_eldritch_reward.visibility = View.VISIBLE
            tv_eldritch_reward.visibility = View.VISIBLE
            tv_eldritch_reward.text = item.rewards.eldritch.toString()
        } else {
            iv_eldritch_reward.visibility = View.GONE
            tv_eldritch_reward.visibility = View.GONE
        }
        if (item.rewards.order > 0) {
            iv_order_reward.visibility = View.VISIBLE
            tv_order_reward.visibility = View.VISIBLE
            tv_order_reward.text = item.rewards.order.toString()
        } else {
            iv_order_reward.visibility = View.GONE
            tv_order_reward.visibility = View.GONE
        }
        if (item.rewards.primordial > 0) {
            iv_primordial_reward.visibility = View.VISIBLE
            tv_primordial_reward.visibility = View.VISIBLE
            tv_primordial_reward.text = item.rewards.primordial.toString()
        } else {
            iv_primordial_reward.visibility = View.GONE
            tv_primordial_reward.visibility = View.GONE
        }
        if (item.rewards.ascendant == 0
            && item.rewards.chaos == 0
            && item.rewards.eldritch == 0
            && item.rewards.order == 0
            && item.rewards.primordial == 0
        ) {
            vg_reward.visibility = View.GONE
            tv_reward.visibility = View.GONE
        } else {
            vg_reward.visibility = View.VISIBLE
            tv_reward.visibility = View.VISIBLE
        }
    }

    private fun setupButton(
        item: Constellation,
        resources: Resource,
        listener: IConstellationView,
        enforceRules: Boolean
    ) {
        if (item.selected) {
            btn_remove.visibility = View.VISIBLE
            btn_add.visibility = View.GONE
            btn_remove.setOnClickListener { listener.onRemoveItemClicked(item) }
            vg_header.setBackgroundColor(ContextCompat.getColor(context, R.color.selectedGreen))
        } else {
            btn_remove.visibility = View.GONE
            if (item.isAvailable(resources) || !enforceRules) {
                btn_add.visibility = View.VISIBLE
                btn_add.setOnClickListener { listener.onAddItemClicked(item) }
                vg_header.setBackgroundColor(ContextCompat.getColor(context, R.color.availableCream))
            } else if (item.steppingStone != null) {
                btn_add.visibility = View.VISIBLE
                btn_add.setOnClickListener { listener.onAddItemClicked(item) }
                vg_header.setBackgroundColor(ContextCompat.getColor(context, R.color.unavailableRed))
            } else {
                btn_add.visibility = View.GONE
                vg_header.setBackgroundColor(ContextCompat.getColor(context, R.color.unavailableRed))
            }
        }
    }

    private fun setupStar(
        item: Constellation,
        listener: IConstellationView
    ) {
        cb_starred.isChecked = item.starred
    }

    private fun getDrawable(name: String): Drawable {
        val resources = context.resources
        val resourceId = resources.getIdentifier(
            name, "drawable",
            context.packageName
        )
        return resources.getDrawable(resourceId)
    }
}