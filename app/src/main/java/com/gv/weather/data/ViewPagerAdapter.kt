package com.gv.weather.data

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import androidx.collection.forEach
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import androidx.viewpager2.adapter.StatefulAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gv.weather.core.getSharedPrefs


abstract class ViewPagerAdapter(fragment: Fragment) :
    RecyclerView.Adapter<ViewPagerAdapter.FragmentViewHolder>(), StatefulAdapter {

    private val mFragmentManager = fragment.childFragmentManager
    private val mLifecycle = fragment.viewLifecycleOwner.lifecycle

    // State saving config
    private val keyPrefixFragment = "f#"
    private val keyPrefixState = "s#"

    // Fragment GC config
    private val graceWindowTimeMs = 10_000

    // Fragment bookkeeping
    private val mFragments = LongSparseArray<Fragment>()
    private val mSavedStates = LongSparseArray<Fragment.SavedState>()
    private val mItemIdToViewHolder = LongSparseArray<Int>()

    private var mFragmentMaxLifecycleEnforcer: FragmentMaxLifecycleEnforcer? = null

    // Fragment GC
    private var mIsInGracePeriod = false
    private var mHasStaleFragments = false

    init { super.setHasStableIds(true) }

    private val prefs by lazy { fragment.requireContext().getSharedPrefs("MainSettings") }

    val tabs
        get() = prefs.getString("tabs", null)?.split(";")
            ?.map { it.trim('(', ')')
                .run { Pair(substringBefore(",").toInt(), substringAfter(",")) } }
            ?: emptyList()


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        if (mFragmentMaxLifecycleEnforcer != null) throw IllegalArgumentException()
        mFragmentMaxLifecycleEnforcer = FragmentMaxLifecycleEnforcer()
        mFragmentMaxLifecycleEnforcer?.register(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mFragmentMaxLifecycleEnforcer?.unregister(recyclerView)
        mFragmentMaxLifecycleEnforcer = null
    }

    abstract fun createFragment(position: Int): Fragment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FragmentViewHolder.create(parent)

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int) {
        val itemId = holder.itemId
        val viewHolderId = holder.container.id
        val boundItemId = itemForViewHolder(viewHolderId)
        if (boundItemId != null && boundItemId != itemId) {
            removeFragment(boundItemId)
            mItemIdToViewHolder.remove(boundItemId)
        }

        mItemIdToViewHolder.put(itemId, viewHolderId) // this might overwrite an existing entry
        ensureFragment(position)

        val container = holder.container
        if (ViewCompat.isAttachedToWindow(container)) {
            if (container.parent != null)
                throw IllegalStateException("Design assumption violated.")
            container.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    if (container.parent != null) {
                        container.removeOnLayoutChangeListener(this)
                        placeFragmentInViewHolder(holder)
                    }
                }

            })
        }

        gcFragments()
    }

    private fun gcFragments() {
        if (!mHasStaleFragments || shouldDelayFragmentTransactions()) return

        // Remove Fragments for items that are no longer part of the data-set
        val toRemove = ArraySet<Long>()
        mFragments.forEach { key, _ ->
            if (!containsItem(key)) {
                toRemove.add(key)
                mItemIdToViewHolder.remove(key) // in case they're still bound
            }
        }

        // Remove Fragments that are not bound anywhere -- pending a grace period
        if (!mIsInGracePeriod) {
            mHasStaleFragments = false // we've executed all GC checks

            mFragments.forEach { key, _ -> if (!isFragmentViewBound(key)) toRemove.add(key) }
        }

        toRemove.forEach { removeFragment(it) }
    }

    private fun isFragmentViewBound(itemId: Long): Boolean {
        if (mItemIdToViewHolder.containsKey(itemId)) return true

        val fragment = mFragments.get(itemId) ?: return false

        val view = fragment.view ?: return false

        return view.parent != null
    }

    private fun itemForViewHolder(viewHolderId: Int): Long? {
        var boundItemId: Long? = null
        mItemIdToViewHolder.forEach { key, value ->
            if (value == viewHolderId) {
                if (boundItemId != null)
                    throw IllegalStateException("Design assumption violated: "
                            + "a ViewHolder can only be bound to one item at a time.")
                boundItemId = key
            }
        }
        return boundItemId
    }

    private fun ensureFragment(position: Int) {
        val itemId = getItemId(position)

        /*if (mFragments.containsKey(itemId)) {
            val fName = mFragments[itemId]?.javaClass?.canonicalName?.let {
                if (it == "com.gv.notes.notes.ui.NotesFragment") "notes" else "tasks" } ?: ""
            if (position == 0)
                if (primaryScreen != fName) removeFragment(itemId) else return
            else
                if (primaryScreen == fName) removeFragment(itemId) else return
        }*/

        if (!mFragments.containsKey(itemId)) {
            val newFragment = createFragment(position)
            newFragment.setInitialSavedState(mSavedStates[itemId])
            mFragments.put(itemId, newFragment)
        }
    }

    override fun onViewAttachedToWindow(holder: FragmentViewHolder) {
        placeFragmentInViewHolder(holder)
        gcFragments()
    }

    /**
     * @param holder that has been bound to a Fragment in the {@link #onBindViewHolder} stage.
     */
    private fun placeFragmentInViewHolder(holder: FragmentViewHolder) {
        val fragment = mFragments.get(holder.itemId)
            ?: throw IllegalStateException("Design assumption violated.")
        val container = holder.container
        val view = fragment.view

        if (!fragment.isAdded && view != null)
            throw IllegalStateException("Design assumption violated.")

        if (fragment.isAdded && view == null) {
            scheduleViewAttach(fragment, container)
            return
        }

        if (fragment.isAdded && fragment.requireView().parent != null) {
            if (fragment.requireView().parent != container)
                addViewToContainer(fragment.requireView(), container)
            return
        }

        if (fragment.isAdded) {
            addViewToContainer(fragment.requireView(), container)
            return
        }

        if (!shouldDelayFragmentTransactions()) {
            scheduleViewAttach(fragment, container)
            mFragmentManager.beginTransaction()
                    .add(fragment, "f" + holder.itemId)
                    .setMaxLifecycle(fragment, STARTED)
                    .commitNow()
            mFragmentMaxLifecycleEnforcer?.updateFragmentMaxLifecycle(false)
        } else {
            if (mFragmentManager.isDestroyed) return // nothing we can do
            mLifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (shouldDelayFragmentTransactions()) return
                    source.lifecycle.removeObserver(this)
                    if (ViewCompat.isAttachedToWindow(holder.container))
                        placeFragmentInViewHolder(holder)
                }
            })
        }
    }

    private fun scheduleViewAttach(fragment: Fragment, container: FrameLayout) {
        mFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentViewCreated(
                        fm: FragmentManager,
                        f: Fragment,
                        v: View,
                        savedInstanceState: Bundle?
                    ) {
                        if (f == fragment) {
                            fm.unregisterFragmentLifecycleCallbacks(this)
                            addViewToContainer(v, container)
                        }
                    }

                }, false)
    }

    private fun addViewToContainer(v: View, container: FrameLayout) {
        if (container.childCount > 1)
            throw IllegalStateException("Design assumption violated.")

        if (v.parent == container) return

        if (container.childCount > 0) container.removeAllViews()

        if (v.parent != null) (v.parent as ViewGroup).removeView(v)

        container.addView(v)
    }

    override fun onViewRecycled(holder: FragmentViewHolder) {
        val viewHolderId = holder.container.id
        val boundItemId = itemForViewHolder(viewHolderId) // item currently bound to the VH
        if (boundItemId != null) {
            removeFragment(boundItemId)
            mItemIdToViewHolder.remove(boundItemId)
        }
    }

    override fun onFailedToRecycleView(holder: FragmentViewHolder) = true

    private fun removeFragment(itemId: Long) {
        mFragments.get(itemId)?.run {
            if (view != null) {
                val viewParent = requireView().parent
                if (viewParent != null) (viewParent as FrameLayout).removeAllViews()
            }

            if (!containsItem(itemId)) mSavedStates.remove(itemId)

            if (!isAdded) {
                mFragments.remove(itemId)
                return
            }

            if (shouldDelayFragmentTransactions()) {
                mHasStaleFragments = true
                return
            }

            if (isAdded && containsItem(itemId))
                mSavedStates.put(itemId, mFragmentManager.saveFragmentInstanceState(this))
            mFragmentManager.beginTransaction().remove(this).commitNow()
            mFragments.remove(itemId)
        }
    }

    private fun shouldDelayFragmentTransactions() = mFragmentManager.isStateSaved

    override fun getItemId(position: Int) = position.toLong()

    private fun containsItem(itemId: Long) = itemId in 0 until itemCount

    override fun setHasStableIds(hasStableIds: Boolean) {
        throw UnsupportedOperationException(
                "Stable Ids are required for the adapter to function properly, and the adapter "
                        + "takes care of setting the flag.")
    }

    override fun saveState(): Parcelable {
        val savedState = Bundle(mFragments.size() + mSavedStates.size())

        mFragments.forEach { itemId, _ ->
            val fragment = mFragments.get(itemId)
            if (fragment != null && fragment.isAdded) {
                val key = createKey(keyPrefixFragment, itemId)
                mFragmentManager.putFragment(savedState, key, fragment)
            }
        }

        mSavedStates.forEach { itemId, _ ->
            if (containsItem(itemId)) {
                val key = createKey(keyPrefixState, itemId)
                savedState.putParcelable(key, mSavedStates.get(itemId))
            }
        }

        return savedState
    }

    override fun restoreState(savedState: Parcelable) {
        if (!mSavedStates.isEmpty || !mFragments.isEmpty)
            throw IllegalStateException(
                "Expected the adapter to be 'fresh' while restoring state.")

        val bundle = savedState as Bundle
        if (bundle.classLoader == null) bundle.classLoader = javaClass.classLoader

        bundle.keySet().forEach { key ->
            if (isValidKey(key, keyPrefixFragment)) {
                val itemId = parseIdFromKey(key, keyPrefixFragment)
                val fragment = mFragmentManager.getFragment(bundle, key)
                mFragments.put(itemId, fragment)
                return@forEach
            }

            if (isValidKey(key, keyPrefixState)) {
                val itemId = parseIdFromKey(key, keyPrefixState)
                val state: Fragment.SavedState? = bundle.getParcelable(key)
                if (containsItem(itemId)) mSavedStates.put(itemId, state)
                return@forEach
            }

            throw IllegalArgumentException("Unexpected key in savedState: $key")
        }

        if (!mFragments.isEmpty) {
            mHasStaleFragments = true
            mIsInGracePeriod = true
            gcFragments()
            scheduleGracePeriodEnd()
        }
    }

    private fun scheduleGracePeriodEnd() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            mIsInGracePeriod = false
            gcFragments() // good opportunity to GC
        }

        mLifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    handler.removeCallbacks(runnable)
                    source.lifecycle.removeObserver(this)
                }
            }
        })

        handler.postDelayed(runnable, graceWindowTimeMs.toLong())
    }

    // Helper function for dealing with save / restore state
    private fun createKey(prefix: String, id: Long) = prefix + id

    // Helper function for dealing with save / restore state
    private fun isValidKey(key: String, prefix: String) =
        key.startsWith(prefix) && key.length > prefix.length

    // Helper function for dealing with save / restore state
    private fun parseIdFromKey(key: String, prefix: String) =
        key.substring(prefix.length).toLong(10)

    /**
     * Pauses (STARTED) all Fragments that are attached and not a primary item.
     * Keeps primary item Fragment RESUMED.
     */
    inner class FragmentMaxLifecycleEnforcer {
        private var mPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
        private var mDataObserver: RecyclerView.AdapterDataObserver? = null
        private var mLifecycleObserver: LifecycleEventObserver? = null
        private var mViewPager: ViewPager2? = null

        private var mPrimaryItemId = NO_ID

        fun register(recyclerView: RecyclerView) {
            mViewPager = inferViewPager(recyclerView)

            // signal 1 of 3: current item has changed
            mPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    updateFragmentMaxLifecycle(false)
                }

                override fun onPageSelected(position: Int) {
                    updateFragmentMaxLifecycle(false)
                }
            }
            mViewPager?.registerOnPageChangeCallback(mPageChangeCallback as ViewPager2.OnPageChangeCallback)

            // signal 2 of 3: underlying data-set has been updated
            mDataObserver = object : DataSetChangeObserver() {
                override fun onChanged() {
                    updateFragmentMaxLifecycle(true)
                }
            }
            registerAdapterDataObserver(mDataObserver as DataSetChangeObserver)

            // signal 3 of 3: we may have to catch-up after being in a lifecycle state that
            // prevented us to perform transactions
            mLifecycleObserver = LifecycleEventObserver { _, _ -> updateFragmentMaxLifecycle(false) }
            mLifecycle.addObserver(mLifecycleObserver as LifecycleObserver)
        }

        fun unregister(recyclerView: RecyclerView) {
            val viewPager = inferViewPager(recyclerView)
            viewPager.unregisterOnPageChangeCallback(mPageChangeCallback as ViewPager2.OnPageChangeCallback)
            unregisterAdapterDataObserver(mDataObserver as DataSetChangeObserver)
            mLifecycle.removeObserver(mLifecycleObserver as LifecycleObserver)
            mViewPager = null
        }

        fun updateFragmentMaxLifecycle(dataSetChanged: Boolean) {
            if (shouldDelayFragmentTransactions()) return

            if (mViewPager?.scrollState != ViewPager2.SCROLL_STATE_IDLE) return // do not update while not idle to avoid jitter

            if (mFragments.isEmpty || itemCount == 0) return // nothing to do

            val currentItem = mViewPager?.currentItem ?: 0
            if (currentItem >= itemCount) return

            val currentItemId = getItemId(currentItem)
            if (currentItemId == mPrimaryItemId && !dataSetChanged) return // nothing to do

            val currentItemFragment = mFragments.get(currentItemId)
            if (currentItemFragment == null || !currentItemFragment.isAdded) return

            mPrimaryItemId = currentItemId
            val transaction = mFragmentManager.beginTransaction()

            var toResume: Fragment? = null
            mFragments.forEach { itemId, fragment ->
                if (!fragment.isAdded) return@forEach

                if (itemId != mPrimaryItemId) {
                    transaction.setMaxLifecycle(fragment, STARTED)
                } else {
                    toResume = fragment // itemId map key, so only one can match the predicate
                }

                fragment.setMenuVisibility(itemId == mPrimaryItemId)
            }
            // in case the Fragment wasn't added yet
            toResume?.let { transaction.setMaxLifecycle(it, RESUMED) }

            if (!transaction.isEmpty) transaction.commitNow()
        }

        private fun inferViewPager(recyclerView: RecyclerView): ViewPager2 {
            val parent = recyclerView.parent
            if (parent is ViewPager2) return parent
            throw IllegalStateException("Expected ViewPager2 instance. Got: $parent")
        }
    }

    /**
     * Simplified {@link RecyclerView.AdapterDataObserver} for clients interested in any data-set
     * changes regardless of their nature.
     */
    private abstract class DataSetChangeObserver : RecyclerView.AdapterDataObserver() {

        abstract override fun onChanged()

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) { onChanged() }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onChanged()
        }
    }

    class FragmentViewHolder private constructor(container: FrameLayout) :
        RecyclerView.ViewHolder(container) {
        val container: FrameLayout
            get() = itemView as FrameLayout

        companion object {
            fun create(parent: ViewGroup): FragmentViewHolder {
                val container = FrameLayout(parent.context)
                container.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                container.id = ViewCompat.generateViewId()
                container.isSaveEnabled = false
                return FragmentViewHolder(container)
            }
        }
    }

}