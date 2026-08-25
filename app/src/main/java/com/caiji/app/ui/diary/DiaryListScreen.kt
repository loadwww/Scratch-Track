package com.caiji.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caiji.app.data.db.entity.Diary
import com.caiji.app.data.db.entity.LotteryRecord
import com.caiji.app.ui.theme.InvestRed
import com.caiji.app.ui.theme.ProfitGold
import com.caiji.app.ui.theme.WinGreen
import com.caiji.app.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryListScreen(
    vm: DiaryViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val lotteryRecords by vm.lotteryRecords.collectAsStateWithLifecycle()
    val diaries by vm.diaries.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    // 中奖记录筛选与排序状态
    var selectedMonth by remember { mutableStateOf("全部") }
    var sortBy by remember { mutableStateOf("默认") }
    var sortDescending by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("彩票日记") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Outlined.Add, contentDescription = "添加记录")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("中奖记录 (${lotteryRecords.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("彩票日记 (${diaries.size})") }
                )
            }

            when (selectedTab) {
                0 -> {
                    // 筛选与排序栏
                    FilterSortBar(
                        records = lotteryRecords,
                        selectedMonth = selectedMonth,
                        onMonthChange = { selectedMonth = it },
                        sortBy = sortBy,
                        onSortChange = { sortBy = it },
                        sortDescending = sortDescending,
                        onToggleSortDirection = { sortDescending = !sortDescending }
                    )
                    // 处理后的列表
                    val filteredSorted = remember(lotteryRecords, selectedMonth, sortBy, sortDescending) {
                        filterAndSortRecords(lotteryRecords, selectedMonth, sortBy, sortDescending)
                    }
                    LotteryRecordList(
                        records = filteredSorted,
                        onDelete = { vm.deleteLotteryRecord(it) }
                    )
                }
                1 -> DiaryOnlyList(
                    diaries = diaries,
                    onClick = { onEdit(it) },
                    onDelete = { vm.deleteDiary(it) },
                    onToggleHighlight = { vm.toggleHighlight(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSortBar(
    records: List<LotteryRecord>,
    selectedMonth: String,
    onMonthChange: (String) -> Unit,
    sortBy: String,
    onSortChange: (String) -> Unit,
    sortDescending: Boolean,
    onToggleSortDirection: () -> Unit
) {
    // 从记录中提取可用月份列表
    val monthFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val availableMonths = remember(records) {
        listOf("全部") + records
            .map { monthFmt.format(it.createdAt) }
            .distinct()
            .sortedDescending()
    }

    val sortOptions = listOf("默认", "投入", "盈利", "盈利比")

    var monthExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 月份下拉
        ExposedDropdownMenuBox(
            expanded = monthExpanded,
            onExpandedChange = { monthExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedMonth,
                onValueChange = {},
                readOnly = true,
                label = { Text("月份") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = { monthExpanded = false }
            ) {
                availableMonths.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month) },
                        onClick = {
                            onMonthChange(month)
                            monthExpanded = false
                        }
                    )
                }
            }
        }

        // 排序下拉
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = sortExpanded,
                onExpandedChange = { sortExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = sortBy,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("排序") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onSortChange(option)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
            // 升降序切换按钮
            IconButton(onClick = onToggleSortDirection) {
                Icon(
                    imageVector = if (sortDescending) Icons.Outlined.KeyboardArrowDown
                    else Icons.Outlined.KeyboardArrowUp,
                    contentDescription = if (sortDescending) "降序" else "升序",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun filterAndSortRecords(
    records: List<LotteryRecord>,
    month: String,
    sort: String,
    descending: Boolean
): List<LotteryRecord> {
    val monthFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    // 月份筛选
    val filtered = if (month == "全部") records
    else records.filter { monthFmt.format(it.createdAt) == month }

    // 排序
    val sorted = when (sort) {
        "投入" -> if (descending) filtered.sortedByDescending { it.amount } else filtered.sortedBy { it.amount }
        "盈利" -> if (descending) filtered.sortedByDescending { it.winAmount - it.amount } else filtered.sortedBy { it.winAmount - it.amount }
        "盈利比" -> {
            val cmp = compareBy<LotteryRecord> { if (it.amount > 0) (it.winAmount - it.amount) / it.amount else 0.0 }
            if (descending) filtered.sortedWith(cmp.reversed()) else filtered.sortedWith(cmp)
        }
        else -> if (descending) filtered.sortedByDescending { it.createdAt } else filtered.sortedBy { it.createdAt }
    }
    return sorted
}

@Composable
private fun LotteryRecordList(
    records: List<LotteryRecord>,
    onDelete: (LotteryRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(records, key = { it.id }) { record ->
            LotteryRecordCard(record = record, onDelete = { onDelete(record) })
        }
        if (records.isEmpty()) {
            item {
                Text(
                    "还没有中奖记录，点击右下角添加记录时会自动生成",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun DiaryOnlyList(
    diaries: List<Diary>,
    onClick: (Long) -> Unit,
    onDelete: (Diary) -> Unit,
    onToggleHighlight: (Diary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(diaries, key = { it.id }) { diary ->
            DiaryCard(
                diary = diary,
                onClick = { onClick(diary.id) },
                onDelete = { onDelete(diary) },
                onToggleHighlight = { onToggleHighlight(diary) }
            )
        }
        if (diaries.isEmpty()) {
            item {
                Text(
                    "还没有彩票日记，添加记录时填写标题或图片即可自动生成",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun LotteryRecordCard(
    record: LotteryRecord,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = DateUtils.formatDateTime(record.createdAt),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = InvestRed)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DataBar(label = "投入", value = record.amount, color = InvestRed, modifier = Modifier.weight(1f))
            DataBar(label = "中奖", value = record.winAmount, color = WinGreen, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DiaryCard(
    diary: Diary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleHighlight: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = diary.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleHighlight) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = if (diary.isHighlighted) "取消收藏" else "收藏",
                    tint = if (diary.isHighlighted) ProfitGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = InvestRed)
            }
        }
        Text(
            text = DateUtils.formatDateTime(diary.recordTime),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )
        if (diary.content.isNotBlank()) {
            Text(
                text = diary.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3
            )
        }
    }
}

@Composable
private fun DataBar(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Text(
            text = "¥${"%.2f".format(value)}",
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
