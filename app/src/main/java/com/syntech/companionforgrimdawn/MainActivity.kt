package com.syntech.companionforgrimdawn

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.myriadmobile.searchview.MMSearchView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader

private const val KEY_ENFORCE_RULES = "KEY_ENFORCE_RULES"

class MainActivity : AppCompatActivity(), MMSearchView.ISearchListener, IConstellationView {

    private lateinit var constellations: MutableList<Constellation>
    private lateinit var resources: Resource
    private lateinit var adapter: ConstellationAdapter
    private lateinit var filter: Filter
    private lateinit var pathHistory: String
    private var currentSearch: List<Int>? = null
    private var enforceRules: Boolean
        get() {
            return getSharedPreferences("default", Context.MODE_PRIVATE).getBoolean(KEY_ENFORCE_RULES, true)
        }
        set(value) {
            getSharedPreferences("default", Context.MODE_PRIVATE).edit().putBoolean(KEY_ENFORCE_RULES, value).apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("default", Context.MODE_PRIVATE)
        prefs.getString("save", null)?.let {
            val save = Gson().fromJson(it, Save::class.java)
            resources = save.resources
            constellations = save.devotions
            filter = save.filter
            pathHistory = save.pathHistory
        } ?: run {
            resources = Resource()
            pathHistory = ""
            constellations = mutableListOf()
            filter = Filter()
            val inputStream = assets.open("json/constellations.json")
            val reader = InputStreamReader(inputStream)
            val json = reader.readText()
            val token = object : TypeToken<MutableList<Constellation>>() {}.type

            constellations.addAll(Gson().fromJson<MutableList<Constellation>>(json, token))
        }

        adapter = ConstellationAdapter(resources, this)
        rv_constellations.layoutManager = LinearLayoutManager(this)
        rv_constellations.adapter = adapter
        updateResources()
        updateDataSet()
        setupToolbar()
        setupSearch()
    }

    override fun updateFilteredItems(indices: List<Int>?) {
        currentSearch = indices
        updateDataSet()
    }

    override fun onBackPressed() {
        if (search_view.isShowing) {
            search_view.hideSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onAddItemClicked(item: Constellation) {
        if (enforceRules && constellations.filter { it.selected }.sumBy { it.points } + item.points > 55) {
            MaterialDialog(this)
                .title(text = getString(R.string.too_many_points_title))
                .message(text = getString(R.string.too_many_points_message))
                .positiveButton(android.R.string.ok)
                .show()
        } else if (enforceRules && item.steppingStone != null) {
            item.steppingStone?.let {
                MaterialDialog(this)
                    .title(text = "Use temporary devotion?")
                    .message(text = "You don't meet the requirements to unlock ${item.name}. However, if you add ${it.name} (${it.points} points) temporarily it will allow you to unlock ${item.name}. \n\nDo you want to add ${it.name} temporarily to unlock ${item.name}?")
                    .positiveButton(text = "Yes") { _ ->
                        useSteppingStone(it, item)
                    }
                    .negativeButton(text = "No")
                    .show()
            }
        } else {
            pathHistory += if (!enforceRules) {
                "\n*${pathHistory.lines().size}: Add ${item.name}"
            } else {
                "\n${pathHistory.lines().size}: Add ${item.name}"
            }
            constellations[constellations.indexOf(item)].selected = true
            val current = constellations.filter { it.isAvailable(resources) }
            updateResources()
            updateDataSet()
            save()
            val new = constellations.filter { !current.contains(it) && it.isAvailable(resources) }
            if (new.isNotEmpty()) {
                showNewConstellations(new)
            }
        }
    }

    override fun onRemoveItemClicked(item: Constellation) {
        val tempResources = Resource()
        constellations.filter { it.selected && it != item }.forEach {
            tempResources.ascendant += it.rewards.ascendant
            tempResources.chaos += it.rewards.chaos
            tempResources.eldritch += it.rewards.eldritch
            tempResources.order += it.rewards.order
            tempResources.primordial += it.rewards.primordial
        }
        var isValid = true
        var dependentDevotions = ""
        constellations.filter { it.selected && it != item }.forEach {
            if (it.requirements.ascendant > tempResources.ascendant
                || it.requirements.chaos > tempResources.chaos
                || it.requirements.eldritch > tempResources.eldritch
                || it.requirements.order > tempResources.order
                || it.requirements.primordial > tempResources.primordial
            ) {
                dependentDevotions += "\n>" + it.name
                isValid = false
            }
        }

        if (enforceRules && !isValid) {
            MaterialDialog(this)
                .title(R.string.cannot_remove_title)
                .message(text = getString(R.string.cannot_remove_message) + dependentDevotions)
                .positiveButton(android.R.string.ok)
                .show()
        } else {
            pathHistory += "\n"
            if (!enforceRules) {
                pathHistory += "*"
            }
            pathHistory += "${pathHistory.lines().size}: Remove ${item.name}"
            constellations[constellations.indexOf(item)].selected = false
            updateResources()
            updateDataSet()
            save()
        }
    }

    override fun onItemStarred(item: Constellation) {
        constellations[constellations.indexOf(item)].starred = !constellations[constellations.indexOf(item)].starred
        updateResources()
        updateDataSet()
        save()
    }

    private fun useSteppingStone(tempItem: Constellation, newItem: Constellation) {
        val current = constellations.filter { it.isAvailable(resources) }

        pathHistory += "\n${pathHistory.lines().size}: Add ${tempItem.name}"

        pathHistory += "\n${pathHistory.lines().size}: Add ${newItem.name}"
        constellations[constellations.indexOf(newItem)].selected = true

        pathHistory += "\n${pathHistory.lines().size}: Remove ${tempItem.name}"

        updateResources()
        updateDataSet()
        save()

        val new =
            constellations.filter { it.isAvailable(resources) && !current.contains(it) && !it.selected }
        if (new.isNotEmpty()) {
            showNewConstellations(new)
        }
    }

    private fun updateDataSet() {
        setSteppingStones()
        val dataset = mutableListOf<Constellation>()
        currentSearch?.let {
            dataset.addAll(constellations.slice(it))
        } ?: dataset.addAll(constellations)
        dataset.sortBy { it.name }
        if (enforceRules) {
            dataset.sortByDescending { it.steppingStone != null }
            dataset.sortByDescending { it.isAvailable(resources) }
        }
        dataset.sortByDescending { it.selected }
        adapter.updateEnforceRules(enforceRules)
        if (filter.matchAny) {
            adapter.setItems(
                if (!filter.anySet()) {
                    dataset
                } else {
                    dataset.filter {
                        if (filter.matchRequirements) {
                            (filter.ascendant && it.requirements.ascendant > 0)
                                    || (filter.chaos && it.requirements.chaos > 0)
                                    || (filter.eldritch && it.requirements.eldritch > 0)
                                    || (filter.order && it.requirements.order > 0)
                                    || (filter.primordial && it.requirements.primordial > 0)
                                    || (filter.starred && it.starred)
                        } else {
                            (filter.ascendant && it.rewards.ascendant > 0)
                                    || (filter.chaos && it.rewards.chaos > 0)
                                    || (filter.eldritch && it.rewards.eldritch > 0)
                                    || (filter.order && it.rewards.order > 0)
                                    || (filter.primordial && it.rewards.primordial > 0)
                                    || (filter.starred && it.starred)
                        }

                    }
                }
            )
        } else {
            adapter.setItems(dataset.asSequence().filter {
                if (filter.ascendant) {
                    if (filter.matchRequirements) {
                        it.requirements.ascendant > 0
                    } else {
                        it.rewards.ascendant > 0
                    }
                } else true
            }.filter {
                if (filter.chaos) {
                    if (filter.matchRequirements) {
                        it.requirements.chaos > 0
                    } else {
                        it.rewards.chaos > 0
                    }
                } else true
            }.filter {
                if (filter.eldritch) {
                    if (filter.matchRequirements) {
                        it.requirements.eldritch > 0
                    } else {
                        it.rewards.eldritch > 0
                    }
                } else true
            }.filter {
                if (filter.order) {
                    if (filter.matchRequirements) {
                        it.requirements.order > 0
                    } else {
                        it.rewards.order > 0
                    }
                } else true
            }.filter {
                if (filter.primordial) {
                    if (filter.matchRequirements) {
                        it.requirements.primordial > 0
                    } else {
                        it.rewards.primordial > 0
                    }
                } else true
            }.filter {
                if (filter.starred) it.starred else true
            }.toList())
        }
    }

    private fun showNewConstellations(newConstellations: List<Constellation>) {
        val snackbar =
            Snackbar.make(root, "New Constellations unlocked", Snackbar.LENGTH_INDEFINITE)
        snackbar
            .setAction("View") {
                MaterialDialog(this)
                    .show {
                        title(text = "Unlocked Devotions")
                        message(text = newConstellations.joinToString("\n") { it.name })
                        positiveButton(text = "Done")
                    }
            }
            .show()
    }

    private fun updateResources() {
        resources.ascendant = constellations.filter { it.selected }.sumBy { it.rewards.ascendant }
        tv_ascendant_current.text = resources.ascendant.toString()
        resources.chaos = constellations.filter { it.selected }.sumBy { it.rewards.chaos }
        tv_chaos_current.text = resources.chaos.toString()
        resources.eldritch = constellations.filter { it.selected }.sumBy { it.rewards.eldritch }
        tv_eldritch_current.text = resources.eldritch.toString()
        resources.order = constellations.filter { it.selected }.sumBy { it.rewards.order }
        tv_order_current.text = resources.order.toString()
        resources.primordial = constellations.filter { it.selected }.sumBy { it.rewards.primordial }
        tv_primordial_current.text = resources.primordial.toString()

        val points = constellations.filter { it.selected }.sumBy { it.points }
        tv_points_used.text = "${getString(R.string.points_used)} $points"

        val targetAscendant = constellations.filter { it.starred }.map { it.requirements.ascendant }.max() ?: 0
        tv_ascendant_starred.text = targetAscendant.toString()
        val targetChaos = constellations.filter { it.starred }.map { it.requirements.chaos }.max() ?: 0
        tv_chaos_starred.text = targetChaos.toString()
        val targetEldritch = constellations.filter { it.starred }.map { it.requirements.eldritch }.max() ?: 0
        tv_eldritch_starred.text = targetEldritch.toString()
        val targetOrder = constellations.filter { it.starred }.map { it.requirements.order }.max() ?: 0
        tv_order_starred.text = targetOrder.toString()
        val targetPrimordial = constellations.filter { it.starred }.map { it.requirements.primordial }.max() ?: 0
        tv_primordial_starred.text = targetPrimordial.toString()
        if (targetAscendant == 0 && targetChaos == 0 && targetEldritch == 0 && targetOrder == 0 && targetPrimordial == 0) {
            vg_starred_affinities.visibility = View.GONE
            tv_starred.visibility = View.GONE
        } else {
            vg_starred_affinities.visibility = View.VISIBLE
            tv_starred.visibility = View.VISIBLE
        }
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.toolbar_title)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        toolbar.inflateMenu(R.menu.menu)
        toolbar.menu.forEach {
            it.icon?.setColorFilter(
                ContextCompat.getColor(this, android.R.color.white),
                PorterDuff.Mode.SRC_IN
            )
        }

        toolbar.menu.findItem(R.id.action_enforce_rules).isChecked = enforceRules
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_filter -> {
                    val view = FilterView(this)
                    MaterialDialog(this)
                        .title(text = getString(R.string.filter))
                        .customView(view = view)
                        .positiveButton(R.string.apply_filter) {
                            onFilterApplied(view.filter)
                            save()
                        }
                        .negativeButton(R.string.clear) {
                            filter.ascendant = false
                            filter.chaos = false
                            filter.eldritch = false
                            filter.order = false
                            filter.primordial = false
                            filter.starred = false
                            onFilterApplied(filter)
                            save()
                        }
                        .show()
                    view.setup(filter)
                }
                R.id.action_show_path -> {
                    var pathString = ""
                    pathString += "Ascendant: ${resources.ascendant}"
                    pathString += "\nChaos: ${resources.chaos}"
                    pathString += "\nEldritch: ${resources.eldritch}"
                    pathString += "\nOrder: ${resources.order}"
                    pathString += "\nPrimordial: ${resources.primordial}"
                    pathString += "\n\nActive:"
                    constellations.filter { constellation -> constellation.selected }
                        .sortedBy { constellation -> constellation.name }
                        .forEach { constellation ->
                            pathString += "\n${constellation.name}"
                        }
                    pathString += "\n\nPoints Used: ${constellations.filter { constellation -> constellation.selected }.sumBy { constellation -> constellation.points }}"
                    pathString += "\n\nSteps:"
                    pathHistory.lines().forEach { line ->
                        pathString += "$line\n"
                    }
                    if (pathString.contains("\n*")) {
                        pathString += "\n\n(Steps marked with * were done while rules weren't enforced.)"
                    }

                    MaterialDialog(this)
                        .title(R.string.path)
                        .message(text = pathString)
                        .positiveButton(android.R.string.ok)
                        .negativeButton(R.string.copy) {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Path", pathString)
                            clipboard.primaryClip = clip
                            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                }
                R.id.action_reset -> {
                    pathHistory = ""
                    filter.ascendant = false
                    filter.chaos = false
                    filter.eldritch = false
                    filter.order = false
                    filter.primordial = false
                    filter.starred = false
                    constellations.forEach { constellation ->
                        constellation.selected = false
                        constellation.starred = false
                    }
                    updateResources()
                    updateDataSet()
                    save()
                }
                R.id.action_search -> {
                    search_view.showSearch(null)
                }
                R.id.action_enforce_rules -> {
                    it.isChecked = !it.isChecked
                    enforceRules = it.isChecked
                    updateDataSet()
                }
            }
            false
        }
    }

    private fun setupSearch() {
        search_view.addSearchDataset(constellations.map { it.name })
        search_view.addSearchDataset(constellations.map { it.description })
        search_view.setup(this)
    }

    private fun onFilterApplied(filter: Filter) {
        this.filter = filter
        updateDataSet()
    }

    private fun save() {
        val prefs = getSharedPreferences("default", Context.MODE_PRIVATE)
        val save = Save(resources, constellations, filter, pathHistory)
        val saveJSON = Gson().toJson(save)
        prefs.edit().putString("save", saveJSON).apply()
    }

    private fun setSteppingStones() {
        constellations.forEach { it.steppingStone = null }
        constellations.filter { !it.isAvailable(resources) }.forEach { constellation1 ->
            val steppingStones = mutableListOf<Constellation>()
            constellations.filter {
                it.name != constellation1.name
                        && !it.selected
                        && it.isAvailable(resources)
                        && (constellations.sumBy { constellation -> if (constellation.selected) constellation.points else 0 } + it.points) <= 55
            }.forEach { constellation2 ->
                val tempResources = Resource()
                constellations.filter { it.selected }.forEach {
                    tempResources.ascendant += it.rewards.ascendant
                    tempResources.chaos += it.rewards.chaos
                    tempResources.eldritch += it.rewards.eldritch
                    tempResources.order += it.rewards.order
                    tempResources.primordial += it.rewards.primordial
                }
                tempResources.ascendant += constellation2.rewards.ascendant
                tempResources.chaos += constellation2.rewards.chaos
                tempResources.eldritch += constellation2.rewards.eldritch
                tempResources.order += constellation2.rewards.order
                tempResources.primordial += constellation2.rewards.primordial
                if (constellation1.isAvailable(tempResources)
                    && (constellations.sumBy { if (it.selected) it.points else 0 } + constellation2.points + constellation1.points) <= 55
                ) {
                    tempResources.ascendant += constellation1.rewards.ascendant
                    tempResources.chaos += constellation1.rewards.chaos
                    tempResources.eldritch += constellation1.rewards.eldritch
                    tempResources.order += constellation1.rewards.order
                    tempResources.primordial += constellation1.rewards.primordial
                    tempResources.ascendant -= constellation2.rewards.ascendant
                    tempResources.chaos -= constellation2.rewards.chaos
                    tempResources.eldritch -= constellation2.rewards.eldritch
                    tempResources.order -= constellation2.rewards.order
                    tempResources.primordial -= constellation2.rewards.primordial
                    if (constellation1.isAvailable(tempResources)) {
                        steppingStones.add(constellation2)
                    }
                }
            }
            constellation1.steppingStone = steppingStones.minBy { it.points }
        }
    }
}