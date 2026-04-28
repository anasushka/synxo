const STORAGE_KEY = "synxo-demo-auth";

const PROFILE_STATES = {
	DEEP_SEARCH: "Deep Search",
	LIGHT_TALK: "Light Talk",
	GHOST_MODE: "Ghost Mode"
};

const STRATEGIES = {
	RECOMMENDATION: "Recommendation",
	PROXIMITY: "Proximity"
};

const VIEW_META = {
	home: {
		kicker: "Обзор",
		title: "Твое пространство внутри Synxo",
		copy: "Здесь видно текущий статус, быстрый вход в подборку и первые карточки профилей."
	},
	discover: {
		kicker: "Люди",
		title: "Подбор по интересам и расстоянию",
		copy: "Переключай стратегию мэтчинга, смотри профили и сразу переходи в диалог."
	},
	chats: {
		kicker: "Чаты",
		title: "Личные сообщения",
		copy: "Диалоги собраны в отдельной левой колонке, а новые контакты можно начать из подборки."
	},
	profile: {
		kicker: "Профиль",
		title: "Анкета и фото",
		copy: "Управляй состоянием видимости, обновляй интересы и загружай фото профиля."
	},
	access: {
		kicker: "Доступ",
		title: "Вход и регистрация",
		copy: "Логин и регистрация разведены по разным зонам, чтобы не путаться."
	}
};

const PRESETS = {
	alina: {
		displayName: "Alina",
		age: 24,
		email: "alina@example.com",
		password: "password123",
		city: "Minsk",
		bio: "Люблю живую музыку, поздние завтраки и длинные прогулки.",
		interests: "music, coffee, walks",
		state: "DEEP_SEARCH",
		latitude: 53.9006,
		longitude: 27.559
	},
	marta: {
		displayName: "Marta",
		age: 27,
		email: "marta@example.com",
		password: "password123",
		city: "Minsk",
		bio: "Собираю книжные полки, люблю выставки и уютные кафе.",
		interests: "books, coffee, design",
		state: "LIGHT_TALK",
		latitude: 53.9122,
		longitude: 27.5796
	},
	sofia: {
		displayName: "Sofia",
		age: 25,
		email: "sofia@example.com",
		password: "password123",
		city: "Vilnius",
		bio: "Путешествую налегке, рисую по утрам и иногда исчезаю из ленты.",
		interests: "travel, design, books",
		state: "GHOST_MODE",
		latitude: 54.6872,
		longitude: 25.2797
	}
};

const state = {
	auth: loadStoredAuth(),
	user: null,
	profile: null,
	matches: [],
	chats: [],
	conversation: [],
	activeView: "access",
	strategy: "RECOMMENDATION",
	selectedChatUserId: null
};

const elements = {
	navItems: Array.from(document.querySelectorAll("[data-view]")),
	views: Array.from(document.querySelectorAll(".view")),
	noticeBanner: document.getElementById("notice-banner"),
	connectionDot: document.getElementById("connection-dot"),
	connectionLabel: document.getElementById("connection-label"),
	sidebarSession: document.getElementById("sidebar-session"),
	viewKicker: document.getElementById("view-kicker"),
	viewTitle: document.getElementById("view-title"),
	viewCopy: document.getElementById("view-copy"),
	refreshButton: document.getElementById("refresh-button"),
	logoutButton: document.getElementById("logout-button"),
	homeHero: document.getElementById("home-hero"),
	homeStats: document.getElementById("home-stats"),
	homeMatches: document.getElementById("home-matches"),
	discoverMatchList: document.getElementById("discover-match-list"),
	strategyButtons: Array.from(document.querySelectorAll("[data-strategy]")),
	chatContactList: document.getElementById("chat-contact-list"),
	chatSuggestions: document.getElementById("chat-suggestions"),
	chatHeader: document.getElementById("chat-header"),
	chatThread: document.getElementById("chat-thread"),
	chatForm: document.getElementById("chat-form"),
	chatRecipientId: document.getElementById("chat-recipient-id"),
	chatMessage: document.getElementById("chat-message"),
	profileSummary: document.getElementById("profile-summary"),
	profileForm: document.getElementById("profile-form"),
	photoForm: document.getElementById("photo-form"),
	profileCity: document.getElementById("profile-city"),
	profileInterests: document.getElementById("profile-interests"),
	profileBio: document.getElementById("profile-bio"),
	profileLatitude: document.getElementById("profile-latitude"),
	profileLongitude: document.getElementById("profile-longitude"),
	profilePhotoFile: document.getElementById("profile-photo-file"),
	stateButtons: Array.from(document.querySelectorAll("[data-state]")),
	loginForm: document.getElementById("login-form"),
	loginEmail: document.getElementById("login-email"),
	loginPassword: document.getElementById("login-password"),
	registerForm: document.getElementById("register-form"),
	registerDisplayName: document.getElementById("register-display-name"),
	registerAge: document.getElementById("register-age"),
	registerEmail: document.getElementById("register-email"),
	registerPassword: document.getElementById("register-password"),
	registerCity: document.getElementById("register-city"),
	registerState: document.getElementById("register-state"),
	registerBio: document.getElementById("register-bio"),
	registerInterests: document.getElementById("register-interests"),
	registerLatitude: document.getElementById("register-latitude"),
	registerLongitude: document.getElementById("register-longitude"),
	presetButtons: Array.from(document.querySelectorAll("[data-preset]"))
};

init();

function init() {
	restoreAuthIntoForms();
	bindEvents();
	state.activeView = state.auth.email ? "home" : "access";
	renderAll();

	if (state.auth.email && state.auth.password) {
		refreshApp(false);
	}
}

function bindEvents() {
	elements.navItems.forEach(button => {
		button.addEventListener("click", () => {
			setActiveView(button.dataset.view);
		});
	});

	elements.refreshButton.addEventListener("click", async () => {
		await refreshApp(true);
	});

	elements.logoutButton.addEventListener("click", () => {
		logout();
	});

	elements.loginForm.addEventListener("submit", async event => {
		event.preventDefault();
		await login();
	});

	elements.registerForm.addEventListener("submit", async event => {
		event.preventDefault();
		await registerProfile();
	});

	elements.profileForm.addEventListener("submit", async event => {
		event.preventDefault();
		await saveProfile();
	});

	elements.photoForm.addEventListener("submit", async event => {
		event.preventDefault();
		await uploadProfilePhoto();
	});

	elements.chatForm.addEventListener("submit", async event => {
		event.preventDefault();
		await sendMessage();
	});

	elements.strategyButtons.forEach(button => {
		button.addEventListener("click", async () => {
			state.strategy = button.dataset.strategy;
			renderStrategyButtons();
			if (isAuthenticated()) {
				await loadMatches();
				renderHome();
				renderDiscover();
			}
		});
	});

	elements.stateButtons.forEach(button => {
		button.addEventListener("click", async () => {
			if (!state.profile || state.profile.state === button.dataset.state) {
				return;
			}
			await changeProfileState(button.dataset.state);
		});
	});

	elements.presetButtons.forEach(button => {
		button.addEventListener("click", () => fillRegisterForm(button.dataset.preset));
	});

	document.addEventListener("click", async event => {
		const actionButton = event.target.closest("[data-action]");
		if (!actionButton) {
			return;
		}

		const action = actionButton.dataset.action;
		if (action === "go-access") {
			setActiveView("access");
		}
		if (action === "go-discover") {
			setActiveView("discover");
		}
		if (action === "go-profile") {
			setActiveView("profile");
		}
		if (action === "open-chat") {
			await openChatFromButton(actionButton);
		}
	});
}

async function login() {
	const email = elements.loginEmail.value.trim();
	const password = elements.loginPassword.value;

	if (!email || !password) {
		showNotice("Введи email и пароль для входа.", "error");
		return;
	}

	state.auth = { email, password };
	storeAuth(state.auth);

	try {
		await refreshApp(false);
		state.activeView = "home";
		showNotice("Сессия активирована. Ты внутри Synxo.", "success");
		renderAll();
	} catch (error) {
		clearAuth(false);
		showNotice(error.message, "error");
		renderAll();
	}
}

async function registerProfile() {
	const payload = {
		displayName: elements.registerDisplayName.value.trim(),
		age: Number(elements.registerAge.value),
		email: elements.registerEmail.value.trim(),
		password: elements.registerPassword.value,
		city: elements.registerCity.value.trim(),
		bio: elements.registerBio.value.trim(),
		interests: splitInterests(elements.registerInterests.value),
		state: elements.registerState.value,
		latitude: Number(elements.registerLatitude.value),
		longitude: Number(elements.registerLongitude.value)
	};

	try {
		await apiRequest("/api/auth/register", {
			method: "POST",
			body: payload,
			auth: null
		});

		state.auth = { email: payload.email, password: payload.password };
		storeAuth(state.auth);
		restoreAuthIntoForms();
		await refreshApp(false);
		state.activeView = "profile";
		showNotice("Профиль создан. Теперь можно загрузить фото и открыть ленту.", "success");
		renderAll();
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function refreshApp(showBanner) {
	if (!isAuthenticated()) {
		renderAll();
		return;
	}

	try {
		const [user, profile] = await Promise.all([
			apiRequest("/api/auth/me"),
			apiRequest("/api/profiles/me")
		]);

		state.user = user;
		state.profile = profile;

		await Promise.all([
			loadMatches(false),
			loadChats(false)
		]);

		if (state.selectedChatUserId) {
			await loadConversation(state.selectedChatUserId, false);
		}

		if (showBanner) {
			showNotice("Данные обновлены.", "success");
		}
		renderAll();
	} catch (error) {
		clearAuth(false);
		showNotice(error.message, "error");
		renderAll();
	}
}

async function loadMatches(renderAfter = true) {
	if (!isAuthenticated()) {
		state.matches = [];
		if (renderAfter) {
			renderHome();
			renderDiscover();
		}
		return;
	}

	state.matches = await apiRequest("/api/matches?strategy=" + encodeURIComponent(state.strategy));
	if (renderAfter) {
		renderHome();
		renderDiscover();
	}
}

async function loadChats(renderAfter = true) {
	if (!isAuthenticated()) {
		state.chats = [];
		state.conversation = [];
		if (renderAfter) {
			renderChats();
		}
		return;
	}

	state.chats = await apiRequest("/api/chats");
	if (!state.selectedChatUserId && state.chats.length) {
		state.selectedChatUserId = state.chats[0].userId;
		await loadConversation(state.selectedChatUserId, false);
	}
	if (renderAfter) {
		renderChats();
	}
}

async function loadConversation(userId, renderAfter = true) {
	if (!userId || !isAuthenticated()) {
		state.conversation = [];
		if (renderAfter) {
			renderChats();
		}
		return;
	}

	state.selectedChatUserId = userId;
	state.chatRecipientId.value = String(userId);
	state.conversation = await apiRequest("/api/chats/" + userId + "/messages");
	if (renderAfter) {
		renderChats();
	}
}

async function saveProfile() {
	if (!state.profile) {
		showNotice("Сначала нужно войти в профиль.", "error");
		return;
	}

	const payload = {
		city: elements.profileCity.value.trim(),
		bio: elements.profileBio.value.trim(),
		latitude: Number(elements.profileLatitude.value),
		longitude: Number(elements.profileLongitude.value),
		interests: splitInterests(elements.profileInterests.value)
	};

	try {
		state.profile = await apiRequest("/api/profiles/me", {
			method: "PUT",
			body: payload
		});
		await loadMatches(false);
		renderAll();
		showNotice("Профиль сохранён.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function uploadProfilePhoto() {
	if (!state.profile) {
		showNotice("Сначала войди в аккаунт.", "error");
		return;
	}

	const file = elements.profilePhotoFile.files[0];
	if (!file) {
		showNotice("Выбери файл изображения.", "error");
		return;
	}

	const formData = new FormData();
	formData.append("file", file);

	try {
		state.profile = await apiRequest("/api/profiles/me/photo", {
			method: "POST",
			body: formData,
			bodyType: "form-data"
		});
		await loadMatches(false);
		await loadChats(false);
		renderAll();
		elements.profilePhotoFile.value = "";
		showNotice("Фото профиля обновлено.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function changeProfileState(nextState) {
	try {
		state.profile = await apiRequest("/api/profiles/me/state", {
			method: "PATCH",
			body: { state: nextState }
		});
		await loadMatches(false);
		renderAll();
		showNotice("Состояние профиля обновлено: " + PROFILE_STATES[nextState] + ".", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function sendMessage() {
	const recipientId = Number(elements.chatRecipientId.value);
	const content = elements.chatMessage.value.trim();

	if (!recipientId) {
		showNotice("Выбери чат или начни диалог из карточки профиля.", "error");
		return;
	}
	if (!content) {
		showNotice("Сообщение не может быть пустым.", "error");
		return;
	}

	try {
		await apiRequest("/api/chats/messages", {
			method: "POST",
			body: {
				recipientUserId: recipientId,
				content
			}
		});
		elements.chatMessage.value = "";
		await loadChats(false);
		await loadConversation(recipientId, false);
		renderChats();
		showNotice("Сообщение отправлено.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function openChatFromButton(button) {
	if (!isAuthenticated()) {
		setActiveView("access");
		showNotice("Сначала войди, чтобы открыть диалоги.", "error");
		return;
	}

	const userId = Number(button.dataset.userId);
	if (!userId) {
		return;
	}

	await loadConversation(userId, false);
	setActiveView("chats");
	renderChats();
}

function setActiveView(viewName) {
	state.activeView = viewName;
	renderViews();
	renderHeader();
	renderSidebar();
}

function logout() {
	clearAuth(true);
	state.user = null;
	state.profile = null;
	state.matches = [];
	state.chats = [];
	state.conversation = [];
	state.selectedChatUserId = null;
	state.activeView = "access";
	restoreAuthIntoForms();
	renderAll();
	showNotice("Сессия завершена.", "success");
}

function clearAuth(clearLoginFields) {
	state.auth = { email: "", password: "" };
	removeStoredAuth();
	if (clearLoginFields) {
		elements.loginEmail.value = "";
		elements.loginPassword.value = "";
	}
}

function renderAll() {
	renderViews();
	renderHeader();
	renderSidebar();
	renderHome();
	renderDiscover();
	renderChats();
	renderProfile();
	renderAccess();
}

function renderViews() {
	elements.views.forEach(view => {
		view.classList.toggle("is-active", view.id === "view-" + state.activeView);
	});
	elements.navItems.forEach(button => {
		button.classList.toggle("is-active", button.dataset.view === state.activeView);
	});
}

function renderHeader() {
	const meta = VIEW_META[state.activeView];
	elements.viewKicker.textContent = meta.kicker;
	elements.viewTitle.textContent = meta.title;
	elements.viewCopy.textContent = meta.copy;
	elements.connectionDot.classList.toggle("is-online", isAuthenticated());
	elements.connectionLabel.textContent = isAuthenticated() ? state.auth.email : "Гость";
	elements.logoutButton.classList.toggle("hidden", !isAuthenticated());
}

function renderSidebar() {
	if (!isAuthenticated() || !state.user) {
		elements.sidebarSession.innerHTML = "<p>Гость</p><strong>Сессия не активна</strong>";
		return;
	}

	elements.sidebarSession.innerHTML = `
		<p>Текущая сессия</p>
		<strong>${escapeHtml(state.user.displayName)}</strong>
		<p>${escapeHtml(state.user.email)}</p>
	`;
}

function renderHome() {
	if (!isAuthenticated() || !state.user || !state.profile) {
		elements.homeHero.innerHTML = `
			<div class="hero-grid">
				<div class="hero-copy">
					<p class="section-kicker">Старт</p>
					<h3>Собери понятный профиль и проверь MVP без лишней путаницы.</h3>
					<p>Сначала зарегистрируйся, потом открой вкладки "Люди", "Чаты" и "Профиль". Все основные действия теперь разведены по отдельным экранам.</p>
					<div class="hero-actions">
						<button class="primary-button" data-action="go-access" type="button">Перейти к доступу</button>
					</div>
				</div>
				<div class="hero-side">
					<p>Что уже готово</p>
					<ul>
						<li>Регистрация и вход</li>
						<li>Подбор профилей</li>
						<li>Чаты между пользователями</li>
						<li>Фото и состояние профиля</li>
					</ul>
				</div>
			</div>
		`;
		elements.homeStats.innerHTML = "";
		elements.homeMatches.innerHTML = guestEmpty("После входа здесь появятся карточки подходящих профилей.");
		return;
	}

	elements.homeHero.innerHTML = `
		<div class="hero-grid">
			<div class="hero-copy">
				<p class="section-kicker">Сессия активна</p>
				<h3>${escapeHtml(state.user.displayName)}, твой профиль уже в сети.</h3>
				<p>Текущее состояние: ${escapeHtml(PROFILE_STATES[state.profile.state])}. Можешь посмотреть подборку, написать в чат или обновить анкету.</p>
				<div class="hero-actions">
					<button class="primary-button" data-action="go-discover" type="button">Открыть ленту</button>
					<button class="secondary-button" data-action="go-profile" type="button">Перейти в профиль</button>
				</div>
			</div>
			<div class="hero-side">
				<div class="person-chip">
					${avatarMarkup(state.user.displayName, state.profile.photoUrl, "large")}
					<div class="profile-hero-copy">
						<h3>${escapeHtml(state.user.displayName)}</h3>
						<p>${escapeHtml(state.profile.city)} · ${escapeHtml(String(state.user.age))} лет</p>
						<p>${escapeHtml(state.profile.bio || "Добавь описание профиля в разделе 'Профиль'.")}</p>
					</div>
				</div>
			</div>
		</div>
	`;

	elements.homeStats.innerHTML = `
		<div class="stat-card">
			<p>Состояние</p>
			<strong>${escapeHtml(PROFILE_STATES[state.profile.state])}</strong>
		</div>
		<div class="stat-card">
			<p>Матчи</p>
			<strong>${escapeHtml(String(state.matches.length))}</strong>
		</div>
		<div class="stat-card">
			<p>Чаты</p>
			<strong>${escapeHtml(String(state.chats.length))}</strong>
		</div>
		<div class="stat-card">
			<p>Интересы</p>
			<strong>${escapeHtml(String(state.profile.interests.length))}</strong>
		</div>
	`;

	elements.homeMatches.innerHTML = state.matches.length
		? state.matches.slice(0, 3).map(matchCardMarkup).join("")
		: guestEmpty("Пока подборка пустая. Создай ещё один профиль с похожими интересами или смени стратегию.");
}

function renderDiscover() {
	renderStrategyButtons();

	if (!isAuthenticated()) {
		elements.discoverMatchList.innerHTML = guestEmpty("Лента откроется после входа.");
		return;
	}

	elements.discoverMatchList.innerHTML = state.matches.length
		? state.matches.map(matchCardMarkup).join("")
		: guestEmpty("Пока нет подходящих профилей. Попробуй другую стратегию или создай дополнительные аккаунты для теста.");
}

function renderChats() {
	if (!isAuthenticated()) {
		elements.chatContactList.innerHTML = guestEmpty("После входа тут появятся диалоги.");
		elements.chatSuggestions.innerHTML = "";
		elements.chatHeader.innerHTML = "";
		elements.chatThread.innerHTML = guestEmpty("Сначала войди и выбери профиль, чтобы начать переписку.");
		elements.chatRecipientId.value = "";
		return;
	}

	ensureSelectedChat();
		const chatContactsMarkup = state.chats.length
		? state.chats.map(chatPreviewMarkup).join("")
		: guestEmpty("У тебя пока нет диалогов. Начни чат из карточки в ленте.");
	elements.chatContactList.innerHTML = chatContactsMarkup;

	const suggestions = buildChatSuggestions();
	elements.chatSuggestions.innerHTML = suggestions.length
		? `
			<div class="section-head compact">
				<div>
					<p class="section-kicker">Подсказки</p>
					<h3>Кому написать</h3>
				</div>
			</div>
			${suggestions.map(chatSuggestionMarkup).join("")}
		`
		: "";

	const selectedTarget = findCurrentChatTarget();
	if (!selectedTarget) {
		elements.chatHeader.innerHTML = "";
		elements.chatThread.innerHTML = guestEmpty("Выбери диалог слева или начни переписку из подборки.");
		elements.chatRecipientId.value = "";
		return;
	}

	elements.chatRecipientId.value = String(selectedTarget.userId);
	elements.chatHeader.innerHTML = `
		${avatarMarkup(selectedTarget.displayName, selectedTarget.photoUrl)}
		<div>
			<strong>${escapeHtml(selectedTarget.displayName)}</strong>
			<p class="chat-subcopy">${escapeHtml(selectedTarget.lastMessage || selectedTarget.city || "Новый диалог")}</p>
		</div>
	`;

	elements.chatThread.innerHTML = state.conversation.length
		? state.conversation.map(messageBubbleMarkup).join("")
		: guestEmpty("Диалог пустой. Можно отправить первое сообщение прямо сейчас.");
}

function renderProfile() {
	renderStateButtons();

	if (!isAuthenticated() || !state.profile || !state.user) {
		elements.profileSummary.innerHTML = guestEmpty("После входа здесь появится твой профиль, фото и управление состоянием.");
		clearProfileForm();
		return;
	}

	elements.profileSummary.innerHTML = `
		<div class="profile-hero">
			<div class="person-chip">
				${avatarMarkup(state.user.displayName, state.profile.photoUrl, "large")}
				<div class="profile-hero-copy">
					<h3>${escapeHtml(state.user.displayName)}</h3>
					<p>${escapeHtml(state.user.email)}</p>
					<p>${escapeHtml(state.profile.bio || "Добавь немного информации о себе.")}</p>
				</div>
			</div>
			<div class="chip">${escapeHtml(PROFILE_STATES[state.profile.state])}</div>
		</div>
		<div class="profile-meta-grid">
			<div class="meta-card">
				<p>Город</p>
				<strong>${escapeHtml(state.profile.city)}</strong>
			</div>
			<div class="meta-card">
				<p>Возраст</p>
				<strong>${escapeHtml(String(state.user.age))}</strong>
			</div>
			<div class="meta-card">
				<p>Последняя активность</p>
				<strong>${escapeHtml(formatDate(state.profile.lastActiveAt))}</strong>
			</div>
			<div class="meta-card">
				<p>Интересы</p>
				<strong>${escapeHtml(String(state.profile.interests.length))}</strong>
			</div>
		</div>
		<div>
			<p class="section-kicker">Интересы</p>
			<div class="chip-row">${state.profile.interests.map(interest => `<span class="chip">${escapeHtml(interest)}</span>`).join("")}</div>
		</div>
	`;

	elements.profileCity.value = state.profile.city || "";
	elements.profileInterests.value = state.profile.interests.join(", ");
	elements.profileBio.value = state.profile.bio || "";
	elements.profileLatitude.value = state.profile.latitude ?? "";
	elements.profileLongitude.value = state.profile.longitude ?? "";
}

function renderAccess() {
	restoreAuthIntoForms();
}

function renderStrategyButtons() {
	elements.strategyButtons.forEach(button => {
		button.classList.toggle("is-active", button.dataset.strategy === state.strategy);
		button.disabled = !isAuthenticated();
	});
}

function renderStateButtons() {
	elements.stateButtons.forEach(button => {
		button.classList.toggle("is-active", state.profile && button.dataset.state === state.profile.state);
		button.disabled = !isAuthenticated();
	});
}

function ensureSelectedChat() {
	if (state.selectedChatUserId) {
		return;
	}
	if (state.chats.length) {
		state.selectedChatUserId = state.chats[0].userId;
	}
}

function buildChatSuggestions() {
	const chatUserIds = new Set(state.chats.map(chat => chat.userId));
	return state.matches.filter(match => !chatUserIds.has(match.userId));
}

function findCurrentChatTarget() {
	return state.chats.find(chat => chat.userId === state.selectedChatUserId)
		|| state.matches.find(match => match.userId === state.selectedChatUserId)
		|| null;
}

function matchCardMarkup(match) {
	const sharedInterests = match.sharedInterests.length
		? match.sharedInterests.map(interest => `<span class="chip">${escapeHtml(interest)}</span>`).join("")
		: `<span class="chip">Новые интересы</span>`;
	const distanceLabel = match.distanceKm == null ? "n/a" : match.distanceKm.toFixed(1) + " km";

	return `
		<article class="match-card">
			<div class="card-head">
				<div class="person-chip">
					${avatarMarkup(match.displayName, match.photoUrl)}
					<div>
						<h4>${escapeHtml(match.displayName)}</h4>
						<p>${escapeHtml(match.city)} · ${escapeHtml(String(match.age))} лет</p>
						<p>${escapeHtml(PROFILE_STATES[match.state])}</p>
					</div>
				</div>
				<div class="match-stat">
					<span>Distance</span>
					<strong>${escapeHtml(distanceLabel)}</strong>
				</div>
			</div>
			<div>
				<p class="section-kicker">Общие интересы</p>
				<div class="chip-row">${sharedInterests}</div>
			</div>
			<div class="form-actions">
				<button class="primary-button" data-action="open-chat" data-user-id="${match.userId}" type="button">Написать</button>
			</div>
		</article>
	`;
}

function chatPreviewMarkup(chat) {
	return `
		<button class="contact-item ${chat.userId === state.selectedChatUserId ? "is-active" : ""}" data-action="open-chat" data-user-id="${chat.userId}" type="button">
			${avatarMarkup(chat.displayName, chat.photoUrl)}
			<div class="contact-main">
				<strong>${escapeHtml(chat.displayName)}</strong>
				<p class="contact-meta">${escapeHtml(formatDate(chat.lastMessageAt))}</p>
				<p class="contact-message">${escapeHtml(chat.lastMessage)}</p>
			</div>
		</button>
	`;
}

function chatSuggestionMarkup(match) {
	return `
		<button class="contact-item" data-action="open-chat" data-user-id="${match.userId}" type="button">
			${avatarMarkup(match.displayName, match.photoUrl)}
			<div class="contact-main">
				<strong>${escapeHtml(match.displayName)}</strong>
				<p class="contact-meta">${escapeHtml(match.city)} · ${escapeHtml(PROFILE_STATES[match.state])}</p>
			</div>
		</button>
	`;
}

function messageBubbleMarkup(message) {
	return `
		<div class="bubble ${message.outgoing ? "outgoing" : ""}">
			<p>${escapeHtml(message.content)}</p>
			<div class="bubble-meta">${escapeHtml(formatDate(message.createdAt))}</div>
		</div>
	`;
}

function avatarMarkup(name, photoUrl, sizeClass = "") {
	if (photoUrl) {
		return `<div class="avatar ${sizeClass}"><img alt="${escapeHtml(name)}" src="${photoUrl}"></div>`;
	}

	return `<div class="avatar ${sizeClass}">${escapeHtml(initialsFrom(name))}</div>`;
}

function guestEmpty(message) {
	return `<div class="empty-state"><p>${escapeHtml(message)}</p></div>`;
}

function showNotice(message, tone) {
	elements.noticeBanner.textContent = message;
	elements.noticeBanner.classList.remove("is-success", "is-error");
	if (tone === "success") {
		elements.noticeBanner.classList.add("is-success");
	}
	if (tone === "error") {
		elements.noticeBanner.classList.add("is-error");
	}
}

async function apiRequest(path, options = {}) {
	const { method = "GET", body, auth = state.auth, bodyType = "json" } = options;
	const headers = {};

	if (auth && auth.email && auth.password) {
		headers.Authorization = "Basic " + toBase64(auth.email + ":" + auth.password);
	}

	let payload = body;
	if (body !== undefined && bodyType === "json") {
		headers["Content-Type"] = "application/json";
		payload = JSON.stringify(body);
	}

	const response = await fetch(path, {
		method,
		headers,
		body: payload
	});

	const raw = await response.text();
	const parsed = tryParseJson(raw);

	if (!response.ok) {
		throw new Error(extractErrorMessage(response.status, parsed, raw));
	}

	return parsed;
}

function extractErrorMessage(status, payload, raw) {
	if (payload && payload.validationErrors) {
		return Object.values(payload.validationErrors).join(" | ");
	}
	if (payload && payload.message) {
		return payload.message;
	}
	if (payload && payload.error) {
		return payload.error;
	}
	if (status === 401) {
		return "Проверь email и пароль. Сервер не принял авторизацию.";
	}
	return raw || ("Ошибка запроса: HTTP " + status);
}

function loadStoredAuth() {
	try {
		return JSON.parse(localStorage.getItem(STORAGE_KEY)) || { email: "", password: "" };
	} catch (error) {
		return { email: "", password: "" };
	}
}

function storeAuth(auth) {
	localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
}

function removeStoredAuth() {
	localStorage.removeItem(STORAGE_KEY);
}

function restoreAuthIntoForms() {
	elements.loginEmail.value = state.auth.email || "";
	elements.loginPassword.value = state.auth.password || "";
}

function fillRegisterForm(name) {
	const preset = PRESETS[name];
	if (!preset) {
		return;
	}

	elements.registerDisplayName.value = preset.displayName;
	elements.registerAge.value = preset.age;
	elements.registerEmail.value = preset.email;
	elements.registerPassword.value = preset.password;
	elements.registerCity.value = preset.city;
	elements.registerState.value = preset.state;
	elements.registerBio.value = preset.bio;
	elements.registerInterests.value = preset.interests;
	elements.registerLatitude.value = preset.latitude;
	elements.registerLongitude.value = preset.longitude;
	showNotice("Форма заполнена примером " + preset.displayName + ".", "success");
}

function clearProfileForm() {
	elements.profileCity.value = "";
	elements.profileInterests.value = "";
	elements.profileBio.value = "";
	elements.profileLatitude.value = "";
	elements.profileLongitude.value = "";
}

function splitInterests(value) {
	return value
		.split(",")
		.map(item => item.trim())
		.filter(Boolean);
}

function tryParseJson(value) {
	if (!value) {
		return null;
	}
	try {
		return JSON.parse(value);
	} catch (error) {
		return null;
	}
}

function toBase64(value) {
	const bytes = new TextEncoder().encode(value);
	let binary = "";
	bytes.forEach(byte => {
		binary += String.fromCharCode(byte);
	});
	return btoa(binary);
}

function initialsFrom(value) {
	return value
		.split(/\s+/)
		.filter(Boolean)
		.slice(0, 2)
		.map(word => word[0].toUpperCase())
		.join("") || "SX";
}

function formatDate(value) {
	if (!value) {
		return "n/a";
	}
	return new Date(value).toLocaleString("ru-RU", {
		day: "2-digit",
		month: "short",
		hour: "2-digit",
		minute: "2-digit"
	});
}

function isAuthenticated() {
	return Boolean(state.auth.email && state.auth.password);
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#39;");
}
