const AuthShared = window.SynxoShared;

const authElements = {
	notice: document.getElementById("auth-notice"),
	modeButtons: Array.from(document.querySelectorAll("[data-mode]")),
	loginForm: document.getElementById("login-form"),
	registerForm: document.getElementById("register-form"),
	loginEmail: document.getElementById("login-email"),
	loginPassword: document.getElementById("login-password"),
	registerDisplayName: document.getElementById("register-display-name"),
	registerAge: document.getElementById("register-age"),
	registerEmail: document.getElementById("register-email"),
	registerPassword: document.getElementById("register-password"),
	registerCity: document.getElementById("register-city"),
	registerState: document.getElementById("register-state"),
	registerBio: document.getElementById("register-bio"),
	registerLatitude: document.getElementById("register-latitude"),
	registerLongitude: document.getElementById("register-longitude"),
	registerInterestGroups: document.getElementById("register-interest-groups")
};

const authState = {
	mode: "login",
	interestCategories: []
};

initAuthPage();

async function initAuthPage() {
	authState.mode = new URLSearchParams(window.location.search).get("mode") === "register" ? "register" : "login";
	renderMode();
	bindAuthEvents();

	try {
		authState.interestCategories = await AuthShared.apiRequest("/api/interests", { auth: null });
		renderInterestGroups(authElements.registerInterestGroups, authState.interestCategories, []);
	} catch (error) {
		showAuthNotice(error.message, "error");
	}
}

function bindAuthEvents() {
	authElements.modeButtons.forEach(button => {
		button.addEventListener("click", () => {
			authState.mode = button.dataset.mode;
			renderMode();
		});
	});

	authElements.loginForm.addEventListener("submit", async event => {
		event.preventDefault();
		await login();
	});

	authElements.registerForm.addEventListener("submit", async event => {
		event.preventDefault();
		await register();
	});
}

function renderMode() {
	authElements.modeButtons.forEach(button => {
		button.classList.toggle("is-active", button.dataset.mode === authState.mode);
	});
	authElements.loginForm.classList.toggle("hidden", authState.mode !== "login");
	authElements.registerForm.classList.toggle("hidden", authState.mode !== "register");
}

async function login() {
	const email = authElements.loginEmail.value.trim();
	const password = authElements.loginPassword.value;

	if (!email || !password) {
		showAuthNotice("Введи email и пароль.", "error");
		return;
	}

	const auth = { email, password };

	try {
		await AuthShared.apiRequest("/api/auth/me", { auth });
		AuthShared.saveAuth(auth);
		showAuthNotice("Вход выполнен. Открываем приложение.", "success");
		window.location.href = "/app.html";
	} catch (error) {
		showAuthNotice(error.message, "error");
	}
}

async function register() {
	const interests = collectCheckedValues(authElements.registerInterestGroups);
	const payload = {
		displayName: authElements.registerDisplayName.value.trim(),
		age: Number(authElements.registerAge.value),
		email: authElements.registerEmail.value.trim(),
		password: authElements.registerPassword.value,
		city: authElements.registerCity.value.trim(),
		bio: authElements.registerBio.value.trim(),
		interests,
		state: authElements.registerState.value,
		latitude: Number(authElements.registerLatitude.value),
		longitude: Number(authElements.registerLongitude.value)
	};

	try {
		await AuthShared.apiRequest("/api/auth/register", {
			method: "POST",
			body: payload,
			auth: null
		});
		AuthShared.saveAuth({ email: payload.email, password: payload.password });
		showAuthNotice("Аккаунт создан. Переходим в приложение.", "success");
		window.location.href = "/app.html";
	} catch (error) {
		showAuthNotice(error.message, "error");
	}
}

function renderInterestGroups(container, categories, selectedValues) {
	container.innerHTML = categories.map(category => `
		<section class="interest-group">
			<p class="section-label">${AuthShared.escapeHtml(category.label)}</p>
			<div class="interest-options">
				${category.options.map(option => `
					<label class="interest-option">
						<input type="checkbox" value="${AuthShared.escapeHtml(option)}" ${selectedValues.includes(option) ? "checked" : ""}>
						<span>${AuthShared.escapeHtml(option)}</span>
					</label>
				`).join("")}
			</div>
		</section>
	`).join("");
}

function collectCheckedValues(container) {
	return Array.from(container.querySelectorAll("input[type='checkbox']:checked"))
		.map(input => input.value);
}

function showAuthNotice(message, tone) {
	authElements.notice.textContent = message;
	authElements.notice.classList.remove("is-success", "is-error");
	if (tone === "success") {
		authElements.notice.classList.add("is-success");
	}
	if (tone === "error") {
		authElements.notice.classList.add("is-error");
	}
}
