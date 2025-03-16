package org.akhsaul.dicodingevent.ui

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import org.akhsaul.dicodingevent.R
import org.akhsaul.dicodingevent.convertTime
import org.akhsaul.dicodingevent.data.Event
import org.akhsaul.dicodingevent.databinding.FragmentDetailBinding
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private val detailViewModel: DetailViewModel by viewModels()
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val event: Event = requireArguments().let {
            if (Build.VERSION.SDK_INT >= 33) {
                requireNotNull(it.getParcelable(KEY_EVENT_DATA, Event::class.java))
            } else {
                @Suppress("DEPRECATION")
                requireNotNull(it.getParcelable(KEY_EVENT_DATA))
            }
        }
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        detailViewModel.setCurrentEvent(event)
        with(binding) {
            ivCover.load(event.mediaCover)
            tvCategory.text = event.category
            tvName.text = event.name
            tvSummary.text = event.summary
            tvQuota.text = getString(R.string.txt_quota).format(event.quota - event.registrants)
            tvLocation.text = getString(R.string.txt_location).format(event.cityName)
            tvTime.text = makeSomeTextBold(event)
            tvOwner.text = getString(R.string.txt_owner).format(event.ownerName)
            tvDesc.text = HtmlCompat.fromHtml(
                event.description,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            btnLink.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, event.link.toUri()))
            }
            actionShare.setOnClickListener {
                startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Check out this event!")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${event.name}\n\n${event.summary}\n\n${event.link}"
                            )
                        },
                        "Share via"
                    )
                )
            }
            toggleFabFavoriteIcon(detailViewModel.isFavoriteEvent())
            detailViewModel.isFavorite.observe(viewLifecycleOwner) {
                toggleFabFavoriteIcon(it)
                detailViewModel.saveToDatabase(it)
            }
            fabFavorite.setOnClickListener {
                detailViewModel.toggleFavorite()
            }
        }

        return binding.root
    }

    private fun toggleFabFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.fabFavorite.setImageIcon(
                Icon.createWithResource(
                    requireContext(),
                    R.drawable.ic_favorite_24dp
                )
            )
        } else {
            binding.fabFavorite.setImageIcon(
                Icon.createWithResource(
                    requireContext(),
                    R.drawable.ic_no_favorite_24dp
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun makeSomeTextBold(event: Event): Spannable {
        val templateText = getString(R.string.txt_time)
        val beginTime = convertTime(event.beginTime).format()
        val endTime = convertTime(event.endTime).format()
        val fullText = templateText.format(beginTime, endTime)
        val first = templateText.indexOfFirst { it == ':' } + 2
        val second = templateText.indexOfLast { it == ':' } + beginTime.length

        val spannable = SpannableStringBuilder(fullText)
        spannable.setSpan(StyleSpan(Typeface.BOLD), first, first + beginTime.length, 0)
        spannable.setSpan(StyleSpan(Typeface.BOLD), second, fullText.length, 0)

        return spannable
    }

    private fun ZonedDateTime.format(): String {
        val userDateFormat = (DateFormat.getDateFormat(context) as SimpleDateFormat).toPattern()
        val userTimeFormat = (DateFormat.getTimeFormat(context) as SimpleDateFormat).toPattern()
        val localFormatter = DateTimeFormatter.ofPattern(
            "$userDateFormat $userTimeFormat",
            resources.configuration.locales.get(0)
        )
        return format(localFormatter)
    }

    companion object {
        const val KEY_EVENT_DATA = "EVENT_DATA"
    }
}