package com.example.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFillDemo: () -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_auth_hero),
                    contentDescription = "Illustration SpendWise gestion financière hors-ligne",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SpendWise",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Maîtrisez vos dépenses quotidiennes, 100 % hors-ligne et en toute confidentialité.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Connexion",
                            style = MaterialTheme.typography.titleLarge
                        )

                        AnimatedVisibility(visible = state.generalError != null) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.generalError.orEmpty(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = { Text("Adresse e-mail") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Email, contentDescription = null)
                            },
                            isError = state.emailError != null,
                            supportingText = state.emailError?.let { err -> { Text(err) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("toggle_login_password")
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "Masquer le mot de passe"
                                        } else {
                                            "Afficher le mot de passe"
                                        }
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            isError = state.passwordError != null,
                            supportingText = state.passwordError?.let { err -> { Text(err) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Button(
                            onClick = onLoginClick,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_submit_button")
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Se connecter")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = onFillDemo,
                                label = { Text("Compte démo") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Shield,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("fill_demo_chip")
                            )

                            TextButton(
                                onClick = onNavigateToRegister,
                                modifier = Modifier.testTag("go_to_register_button")
                            ) {
                                Text("Créer un compte")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    BackHandler { onBackToLogin() }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Retour à la connexion"
                        )
                    }
                    Text(
                        text = "Créer un compte local",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Vos données et identifiants chiffrés restent stockés uniquement sur cet appareil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedVisibility(visible = state.generalError != null) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.generalError.orEmpty(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = state.name,
                            onValueChange = onNameChange,
                            label = { Text("Nom complet") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            isError = state.nameError != null,
                            supportingText = state.nameError?.let { err -> { Text(err) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = { Text("Adresse e-mail") },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            isError = state.emailError != null,
                            supportingText = state.emailError?.let { err -> { Text(err) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = { Text("Mot de passe (min. 6 caractères)") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = "Basculer la visibilité du mot de passe"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            isError = state.passwordError != null,
                            supportingText = state.passwordError?.let { err -> { Text(err) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = { Text("Confirmer le mot de passe") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            isError = state.confirmPasswordError != null,
                            supportingText = state.confirmPasswordError?.let { err -> { Text(err) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onRegisterClick() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_input")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onRegisterClick,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_submit_button")
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Créer mon compte")
                            }
                        }

                        TextButton(
                            onClick = onBackToLogin,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag("register_to_login_button")
                        ) {
                            Text("Déjà inscrit ? Se connecter")
                        }
                    }
                }
            }
        }
    }
}
