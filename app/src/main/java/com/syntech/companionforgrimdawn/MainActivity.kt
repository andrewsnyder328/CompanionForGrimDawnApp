package com.syntech.companionforgrimdawn

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.myriadmobile.searchview.MMSearchView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), MMSearchView.ISearchListener {

    private lateinit var constellations: MutableList<Constellation>
    private lateinit var resources: Resource
    private lateinit var adapter: ConstellationAdapter
    private lateinit var filter: Filter
    private var currentSearch: List<Int>? = null
    private lateinit var pathHistory: String

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

        adapter = ConstellationAdapter(resources, ::onAddItemClicked, ::onRemoveItemClicked)
        rv_constellations.layoutManager = LinearLayoutManager(this)
        rv_constellations.adapter = adapter
        updateResources()
        updateDataSet()
        setupToolbar()
        setupSearch()
    }

    private fun onAddItemClicked(item: Constellation) {
        if (constellations.filter { it.selected }.sumBy { it.points } + item.points > 55) {
            MaterialDialog(this)
                .title(text = getString(R.string.too_many_points_title))
                .message(text = getString(R.string.too_many_points_message))
                .positiveButton(android.R.string.ok)
                .show()
        } else {
            pathHistory += "\n${pathHistory.lines().size}: Add ${item.name}"
            constellations[constellations.indexOf(item)].selected = true
            updateResources()
            updateDataSet()
            save()
        }
    }

    private fun onRemoveItemClicked(item: Constellation) {
        val tempResources = Resource()
        constellations.filter { it.selected && it != item }.forEach {
            tempResources.ascendant += it.rewards.ascendant
            tempResources.chaos += it.rewards.chaos
            tempResources.eldritch += it.rewards.eldritch
            tempResources.order += it.rewards.order
            tempResources.primordial += it.rewards.primordial
        }
        var isValid = true
        constellations.filter { it.selected && it != item }.forEach {
            if (it.requirements.ascendant > tempResources.ascendant
                || it.requirements.chaos > tempResources.chaos
                || it.requirements.eldritch > tempResources.eldritch
                || it.requirements.order > tempResources.order
                || it.requirements.primordial > tempResources.primordial
            ) {
                isValid = false
            }
        }
        if (isValid) {
            pathHistory += "\n${pathHistory.lines().size}: Remove ${item.name}"
            constellations[constellations.indexOf(item)].selected = false
            updateResources()
            updateDataSet()
            save()
        } else {
            MaterialDialog(this)
                .title(R.string.cannot_remove_title)
                .message(R.string.cannot_remove_message)
                .positiveButton(android.R.string.ok)
                .show()
        }
    }

    private fun updateDataSet() {
        val dataset = mutableListOf<Constellation>()
        currentSearch?.let {
            dataset.addAll(constellations.slice(it))
        } ?: dataset.addAll(constellations)
        dataset.sortBy { it.name }
        dataset.sortByDescending { it.isAvailable(resources) }
        dataset.sortByDescending { it.selected }
        adapter.setItems(dataset.asSequence().filter {
            if (filter.ascendant) it.rewards.ascendant > 0 else true
        }.filter {
            if (filter.chaos) it.rewards.chaos > 0 else true
        }.filter {
            if (filter.eldritch) it.rewards.eldritch > 0 else true
        }.filter {
            if (filter.order) it.rewards.order > 0 else true
        }.filter {
            if (filter.primordial) it.rewards.primordial > 0 else true
        }.toList())
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
        tv_points_used.text = "${getString(R.string.points_used)}$points"
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
                    constellations.forEach { constellation -> constellation.selected = false }
                    updateResources()
                    updateDataSet()
                    save()
                }
                R.id.action_search -> {
                    search_view.showSearch(null)
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

    private fun save() {
        val prefs = getSharedPreferences("default", Context.MODE_PRIVATE)
        val save = Save(resources, constellations, filter, pathHistory)
        val saveJSON = Gson().toJson(save)
        prefs.edit().putString("save", saveJSON).apply()
    }
}