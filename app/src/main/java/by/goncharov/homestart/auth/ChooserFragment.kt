package by.goncharov.homestart.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.goncharov.homestart.R
import by.goncharov.homestart.databinding.FragmentChooserBinding

class ChooserFragment : Fragment() {

    private var _binding: FragmentChooserBinding? = null
    private val binding: FragmentChooserBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChooserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Adapter
        val adapter = MyArrayAdapter(requireContext(), android.R.layout.simple_list_item_2)
        adapter.setDescriptionIds(DESCRIPTION_IDS)

        binding.listView.adapter = adapter
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val actionId = NAV_ACTIONS[position]
            findNavController().navigate(actionId)
        }
    }

    class MyArrayAdapter(
        private val ctx: Context,
        resource: Int,
    ) : ArrayAdapter<String>(ctx, resource, CLASS_NAMES) {
        private var descriptionIds: IntArray? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView

            if (convertView == null) {
                val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(android.R.layout.simple_list_item_2, null)
            }

            // Android internal resource hence can't use synthetic binding
            view?.findViewById<TextView>(android.R.id.text1)?.text = CLASS_NAMES[position]
            view?.findViewById<TextView>(android.R.id.text2)?.setText(descriptionIds!![position])

            return view!!
        }

        fun setDescriptionIds(descriptionIds: IntArray) {
            this.descriptionIds = descriptionIds
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val NAV_ACTIONS = arrayOf(
            R.id.action_google,
            R.id.action_emailpassword,
            R.id.action_firebaseui,
        )
        private val CLASS_NAMES = arrayOf(
            "GoogleSignInFragment",
            "EmailPasswordFragment",
            "FirebaseUIFragment",
        )
        private val DESCRIPTION_IDS = intArrayOf(
            R.string.desc_google_sign_in,
            R.string.desc_emailpassword,
            R.string.desc_firebase_ui,
        )
    }
}
