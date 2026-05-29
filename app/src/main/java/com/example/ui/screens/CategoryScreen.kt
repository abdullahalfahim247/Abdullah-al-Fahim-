package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.components.IconMapper
import com.example.ui.localization.Translator
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    isBangla: Boolean,
    categories: List<Category>,
    onAddCustomCategory: (name: String, type: String, iconName: String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Expense, 1: Income
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCategories = remember(categories, selectedTab) {
        val targetType = if (selectedTab == 0) "EXPENSE" else "INCOME"
        categories.filter { it.type == targetType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Translator.translate("custom_category", isBangla),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add custom category")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // TabRow selector for Expense on the left and Income on the right
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = Translator.translate("expense", isBangla),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = Translator.translate("income", isBangla),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LazyVerticalGrid representing categories items
            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "কোনো ক্যাটেগরি পাওয়া যায়নি" else "No categories found.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCategories) { cat ->
                        CategoryCard(
                            category = cat,
                            isBangla = isBangla,
                            onDeleteClick = { onDeleteCategory(cat) }
                        )
                    }
                }
            }
        }

        // Create Custom Category Dialog
        if (showAddDialog) {
            var newCatName by remember { mutableStateOf("") }
            val types = listOf("EXPENSE", "INCOME")
            var newCatType by remember { mutableStateOf(if (selectedTab == 0) "EXPENSE" else "INCOME") }
            var selectedIconName by remember { mutableStateOf("Category") }
            var categoryAddDropdownExpanded by remember { mutableStateOf(false) }

            val availableIcons = remember {
                listOf(
                    "Fastfood", "ShoppingBag", "DirectionsCar", "PhoneAndroid",
                    "Wifi", "ReceiptLong", "School", "SportsEsports", "LocalPharmacy",
                    "Payments", "Storefront", "Computer", "CardGiftcard", "AccountBalance", "Category"
                )
            }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = Translator.translate("create_category", isBangla),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text(text = Translator.translate("category_name", isBangla)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Type dropdown
                        var typeExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (newCatType == "INCOME") Translator.translate("income", isBangla) else Translator.translate("expense", isBangla),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(text = Translator.translate("type", isBangla)) },
                                trailingIcon = {
                                    IconButton(onClick = { typeExpanded = !typeExpanded }) {
                                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = Translator.translate("expense", isBangla)) },
                                    onClick = {
                                        newCatType = "EXPENSE"
                                        typeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = Translator.translate("income", isBangla)) },
                                    onClick = {
                                        newCatType = "INCOME"
                                        typeExpanded = false
                                    }
                                )
                            }
                        }

                        // Icon selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedIconName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(text = if (isBangla) "আইকন নির্বাচন করুন" else "Select Icon") },
                                leadingIcon = {
                                    Icon(imageVector = IconMapper.getIconByName(selectedIconName), contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { categoryAddDropdownExpanded = !categoryAddDropdownExpanded }) {
                                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = categoryAddDropdownExpanded,
                                onDismissRequest = { categoryAddDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                availableIcons.forEach { icon ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = IconMapper.getIconByName(icon),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(text = icon)
                                            }
                                        },
                                        onClick = {
                                            selectedIconName = icon
                                            categoryAddDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                onAddCustomCategory(newCatName, newCatType, selectedIconName)
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text(text = Translator.translate("add", isBangla))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(text = Translator.translate("cancel", isBangla))
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    isBangla: Boolean,
    onDeleteClick: () -> Unit
) {
    val themeColor = if (category.type == "INCOME") IncomeGreen else ExpenseRed

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconMapper.getIconByName(category.iconName),
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete Custom Category option
                if (category.isCustom) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isBangla) "ডিফল্ট" else "System",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column {
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (category.type == "INCOME") Translator.translate("income", isBangla) else Translator.translate("expense", isBangla),
                    fontSize = 10.sp,
                    color = themeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
