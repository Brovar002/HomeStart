package by.goncharov.homestart.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.goncharov.homestart.fragments.HueColorFragment
import by.goncharov.homestart.fragments.HueLampsFragment
import by.goncharov.homestart.fragments.HueScenesFragment

class HueDetailsTabAdapter(
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HueColorFragment()
            1 -> HueScenesFragment()
            2 -> HueLampsFragment()
            else -> Fragment()
        }
    }

    override fun getItemCount(): Int = 3
}
