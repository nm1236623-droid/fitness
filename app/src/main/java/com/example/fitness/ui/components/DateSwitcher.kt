package com.example.fitness.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness.ui.theme.glassEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateSwitcherRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd"),
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .glassEffect(16.dp)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateChange(date.minusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }

        Box(contentAlignment = Alignment.Center) {
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            date.year,
                            date.monthValue - 1,
                            date.dayOfMonth
                        ).show()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        IconButton(onClick = { onDateChange(date.plusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
        }
    }
}
