const AppShared = window.SynxoShared;

const appState = {
	auth: AppShared.loadAuth(),
	user: null,
	profile: null,
	matches: [],
	chats: [],
	conversation: [],
	interestCategories: [],
	activeView: "home",
	strategy: "RECOMMENDATION",
	selectedChatUserId: null
};

const appElements = {
	sessionCopy: document.getElementById("session-copy"),
	notice: document.getElementById("app-notice"),
	refreshButton: document.getElementById("refresh-button"),
	logoutButton: document.getElementById("logout-button"),
	tabButtons: Array.from(document.querySelectorAll("[data-view]")),
	views: Array.from(document.querySelectorAll(".view-section")),
	strategyButtons: Array.from(document.querySelectorAll("[data-strategy]")),
	homeHero: document.getElementById("home-hero"),
	homeStats: document.getElementById("home-stats"),
	discoverList: document.getElementById("discover-list"),
	chatList: document.getElementById("chat-list"),
	chatHeader: document.getElementById("chat-header"),
	chatThread: document.getElementById("chat-thread"),
	chatForm: document.getElementById("chat-form"),
	chatRecipientId: document.getElementById("chat-recipient-id"),
	chatMessage: document.getElementById("chat-message"),
	profilePhotoPanel: document.getElementById("profile-photo-panel"),
	profileSummaryData: document.getElementById("profile-summary-data"),
	profileForm: document.getElementById("profile-form"),
	profileAge: document.getElementById("profile-age"),
	profileCity: document.getElementById("profile-city"),
	profileState: document.getElementById("profile-state"),
	profileBio: document.getElementById("profile-bio"),
	profileLatitude: document.getElementById("profile-latitude"),
	profileLongitude: document.getElementById("profile-longitude"),
	profileInterestGroups: document.getElementById("profile-interest-groups"),
	photoForm: document.getElementById("photo-form"),
	profilePhotoFile: document.getElementById("profile-photo-file")
};

initApp();

async function initApp() {
	if (!AppShared.hasAuth(appState.auth)) {
		window.location.href = "/auth.html?mode=login";
		return;
	}

	bindAppEvents();

	try {
		appState.interestCategories = await AppShared.apiRequest("/api/interests");
		await refreshApp(false);
		renderAll();
	} catch (error) {
		handleAuthFailure(error);
	}
}

function bindAppEvents() {
	appElements.tabButtons.forEach(button => {
		button.addEventListener("click", () => {
			appState.activeView = button.dataset.view;
			renderViews();
		});
	});

	appElements.strategyButtons.forEach(button => {
		button.addEventListener("click", async () => {
			appState.strategy = button.dataset.strategy;
			renderStrategyButtons();
			await loadMatches();
			renderDiscover();
		});
	});

	appElements.refreshButton.addEventListener("click", async () => {
		await refreshApp(true);
	});

	appElements.logoutButton.addEventListener("click", () => {
		logout();
	});

	appElements.chatForm.addEventListener("submit", async event => {
		event.preventDefault();
		await sendMessage();
	});

	appElements.chatMessage.addEventListener("keydown", async event => {
		if (event.key === "Enter" && !event.shiftKey) {
			event.preventDefault();
			await sendMessage();
		}
	});

	appElements.chatMessage.addEventListener("input", () => {
		autoResizeComposer();
	});

	appElements.profileForm.addEventListener("submit", async event => {
		event.preventDefault();
		await saveProfile();
	});

	appElements.photoForm.addEventListener("submit", async event => {
		event.preventDefault();
		await uploadProfilePhoto();
	});

	document.addEventListener("click", async event => {
		const actionButton = event.target.closest("[data-action]");
		if (!actionButton) {
			return;
		}

		const action = actionButton.dataset.action;
		if (action === "like-profile") {
			await likeProfile(Number(actionButton.dataset.userId));
		}
		if (action === "open-chat") {
			await openChat(Number(actionButton.dataset.userId));
		}
		if (action === "go-discover") {
			appState.activeView = "discover";
			renderViews();
		}
		if (action === "go-chats") {
			appState.activeView = "chats";
			renderViews();
		}
	});
}

async function refreshApp(showBanner) {
	const [user, profile] = await Promise.all([
		AppShared.apiRequest("/api/auth/me"),
		AppShared.apiRequest("/api/profiles/me")
	]);

	appState.user = user;
	appState.profile = profile;

	await Promise.all([
		loadMatches(),
		loadChats()
	]);

	if (appState.selectedChatUserId) {
		await loadConversation(appState.selectedChatUserId);
	}

	renderAll();
	if (showBanner) {
		showNotice("Данные обновлены.", "success");
	}
}

async function loadMatches() {
	appState.matches = await AppShared.apiRequest("/api/matches?strategy=" + encodeURIComponent(appState.strategy));
}

async function loadChats() {
	appState.chats = await AppShared.apiRequest("/api/chats");
	if (!appState.selectedChatUserId && appState.chats.length) {
		appState.selectedChatUserId = appState.chats[0].userId;
	}
}

async function loadConversation(userId) {
	if (!userId) {
		appState.conversation = [];
		return;
	}

	appState.selectedChatUserId = userId;
	appElements.chatRecipientId.value = String(userId);
	appState.conversation = await AppShared.apiRequest("/api/chats/" + userId + "/messages");
}

async function likeProfile(userId) {
	try {
		await AppShared.apiRequest("/api/matches/" + userId + "/like", { method: "POST" });
		await refreshApp(false);
		appState.activeView = "discover";
		renderViews();
		showNotice("Симпатия отправлена.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function openChat(userId) {
	try {
		await loadConversation(userId);
		appState.activeView = "chats";
		renderViews();
		renderChats();
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function sendMessage() {
	const recipientUserId = Number(appElements.chatRecipientId.value);
	const content = appElements.chatMessage.value.trim();

	if (!recipientUserId) {
		showNotice("Сначала выбери чат.", "error");
		return;
	}
	if (!content) {
		showNotice("Сообщение не может быть пустым.", "error");
		return;
	}

	try {
		await AppShared.apiRequest("/api/chats/messages", {
			method: "POST",
			body: { recipientUserId, content }
		});
		appElements.chatMessage.value = "";
		await loadChats();
		await loadConversation(recipientUserId);
		renderChats();
		autoResizeComposer(true);
		showNotice("Сообщение отправлено.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function saveProfile() {
	const payload = {
		age: Number(appElements.profileAge.value),
		city: appElements.profileCity.value.trim(),
		bio: appElements.profileBio.value.trim(),
		latitude: Number(appElements.profileLatitude.value),
		longitude: Number(appElements.profileLongitude.value),
		interests: collectCheckedValues(appElements.profileInterestGroups)
	};

	try {
		await AppShared.apiRequest("/api/profiles/me", {
			method: "PUT",
			body: payload
		});

		await AppShared.apiRequest("/api/profiles/me/state", {
			method: "PATCH",
			body: { state: appElements.profileState.value }
		});

		await refreshApp(false);
		appState.activeView = "profile";
		renderViews();
		showNotice("Профиль обновлен.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function uploadProfilePhoto() {
	const file = appElements.profilePhotoFile.files[0];
	if (!file) {
		showNotice("Сначала выбери изображение.", "error");
		return;
	}

	const formData = new FormData();
	formData.append("file", file);

	try {
		await AppShared.apiRequest("/api/profiles/me/photo", {
			method: "POST",
			body: formData,
			bodyType: "form-data"
		});
		appElements.profilePhotoFile.value = "";
		await refreshApp(false);
		showNotice("Фото профиля обновлено.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

function renderAll() {
	renderHeader();
	renderViews();
	renderStrategyButtons();
	renderHome();
	renderDiscover();
	renderChats();
	renderProfile();
}

function renderHeader() {
	appElements.sessionCopy.textContent = appState.user
		? appState.user.displayName + ", здесь твои совпадения, взаимные лайки и диалоги."
		: "Загружаем твоё пространство.";
}

function renderViews() {
	appElements.views.forEach(view => {
		view.classList.toggle("is-active", view.id === "view-" + appState.activeView);
	});
	appElements.tabButtons.forEach(button => {
		button.classList.toggle("is-active", button.dataset.view === appState.activeView);
	});
}

function renderStrategyButtons() {
	appElements.strategyButtons.forEach(button => {
		button.classList.toggle("is-active", button.dataset.strategy === appState.strategy);
	});
}

function renderHome() {
	const pendingLikes = appState.matches.filter(match => match.likedYou && !match.likedByYou).length;
	const mutualLikes = appState.matches.filter(match => match.mutualLike).length;

	appElements.homeHero.innerHTML = `
		<p class="eyebrow">Текущая сессия</p>
		<h1>${AppShared.escapeHtml(appState.user.displayName)}, здесь видны люди, с которыми у тебя уже есть общие интересы.</h1>
		<p class="hero-text">Сначала отметь понравившийся профиль в ленте. Если симпатия взаимная, человек появится в чатах, и можно будет начать полноценную переписку.</p>
		<div class="hero-actions">
			<button class="primary-button" data-action="go-discover" type="button">Открыть ленту</button>
			${mutualLikes ? '<button class="secondary-button" data-action="go-chats" type="button">Перейти в чаты</button>' : ""}
		</div>
	`;

	appElements.homeStats.innerHTML = `
		<div class="stat-card">
			<p class="muted">Подходящие профили</p>
			<strong>${appState.matches.length}</strong>
		</div>
		<div class="stat-card">
			<p class="muted">Тебя уже лайкнули</p>
			<strong>${pendingLikes}</strong>
		</div>
		<div class="stat-card">
			<p class="muted">Взаимные симпатии</p>
			<strong>${mutualLikes}</strong>
		</div>
		<div class="stat-card">
			<p class="muted">Открытые чаты</p>
			<strong>${appState.chats.length}</strong>
		</div>
	`;
}

function renderDiscover() {
	if (!appState.matches.length) {
		appElements.discoverList.innerHTML = emptyState("Пока в ленте нет профилей с общими интересами. Попробуй обновить интересы в профиле.");
		return;
	}

	appElements.discoverList.innerHTML = appState.matches.map(match => `
		<article class="card">
			<div class="match-head">
				${matchPhotoMarkup(match)}
				<div class="match-copy">
					<div class="match-copy-top">
						<div>
							<h3>${AppShared.escapeHtml(match.displayName)}</h3>
							<p class="muted">${AppShared.escapeHtml(match.city)} · ${AppShared.escapeHtml(String(match.age))} лет</p>
							<p class="muted">${AppShared.escapeHtml(match.state)}</p>
						</div>
						<div class="muted compact-copy">${match.distanceKm == null ? "n/a" : AppShared.escapeHtml(match.distanceKm.toFixed(1) + " km")}</div>
					</div>

					<div>
						<p class="section-label">Общие интересы</p>
						<div class="chip-row">${match.sharedInterests.map(interest => `<span class="chip">${AppShared.escapeHtml(interest)}</span>`).join("")}</div>
					</div>

					<div class="chip-row">
						${match.likedYou && !match.mutualLike ? '<span class="tag warning">Ты нравишься этому человеку</span>' : ""}
						${match.mutualLike ? '<span class="tag success">Взаимная симпатия</span>' : ""}
					</div>

					<div class="form-actions">
						${renderMatchAction(match)}
					</div>
				</div>
			</div>
		</article>
	`).join("");
}

function renderMatchAction(match) {
	if (match.mutualLike) {
		return `<button class="primary-button" data-action="open-chat" data-user-id="${match.userId}" type="button">Открыть чат</button>`;
	}
	if (match.likedByYou) {
		return `<button class="secondary-button" disabled type="button">Лайк отправлен</button>`;
	}
	return `<button class="primary-button" data-action="like-profile" data-user-id="${match.userId}" type="button">${match.likedYou ? "Ответить взаимностью" : "Нравится"}</button>`;
}

function renderChats() {
	if (!appState.chats.length) {
		appElements.chatList.innerHTML = emptyState("Взаимных симпатий пока нет. Они появятся после обмена лайками.");
		appElements.chatHeader.innerHTML = "";
		appElements.chatThread.innerHTML = emptyState("Когда появится взаимность, здесь откроется полноценный диалог.");
		appElements.chatRecipientId.value = "";
		return;
	}

	if (!appState.selectedChatUserId) {
		appState.selectedChatUserId = appState.chats[0].userId;
	}

	appElements.chatList.innerHTML = appState.chats.map(chat => `
		<button class="chat-item ${chat.userId === appState.selectedChatUserId ? "is-active" : ""}" data-action="open-chat" data-user-id="${chat.userId}" type="button">
			<div class="person">
				${avatarMarkup(chat.displayName, chat.photoUrl)}
				<div class="chat-preview">
					<h4>${AppShared.escapeHtml(chat.displayName)}</h4>
					<p class="chat-meta chat-preview-line">${AppShared.escapeHtml(previewMessage(chat.lastMessage))}</p>
					<p class="muted compact-copy">${AppShared.escapeHtml(AppShared.formatDate(chat.lastMessageAt))}</p>
				</div>
			</div>
		</button>
	`).join("");

	const currentChat = appState.chats.find(chat => chat.userId === appState.selectedChatUserId);
	if (!currentChat) {
		appElements.chatHeader.innerHTML = "";
		appElements.chatThread.innerHTML = emptyState("Выбери диалог слева.");
		return;
	}

	appElements.chatRecipientId.value = String(currentChat.userId);
	appElements.chatHeader.innerHTML = `
		<div class="person">
			${avatarMarkup(currentChat.displayName, currentChat.photoUrl, "large")}
			<div>
				<strong>${AppShared.escapeHtml(currentChat.displayName)}</strong>
				<p class="chat-meta">Взаимная симпатия. Можно вести переписку без ограничений.</p>
			</div>
		</div>
	`;

	appElements.chatThread.innerHTML = appState.conversation.length
		? appState.conversation.map(message => `
			<div class="bubble ${message.outgoing ? "outgoing" : ""}">
				<p>${AppShared.escapeHtml(message.content)}</p>
				<time>${AppShared.escapeHtml(AppShared.formatDate(message.createdAt))}</time>
			</div>
		`).join("")
		: emptyState("Диалог открыт. Можно отправить первое сообщение.");

	requestAnimationFrame(() => {
		appElements.chatThread.scrollTop = appElements.chatThread.scrollHeight;
	});
}

function renderProfile() {
	appElements.profilePhotoPanel.innerHTML = `
		<div class="profile-photo-panel">
			${avatarMarkup(appState.user.displayName, appState.profile.photoUrl, "xlarge")}
			<div class="profile-copy">
				<h2>${AppShared.escapeHtml(appState.user.displayName)}</h2>
				<p class="muted">${AppShared.escapeHtml(appState.user.email)}</p>
			</div>
		</div>
	`;

	appElements.profileSummaryData.innerHTML = `
		<div class="profile-copy">
			<p class="eyebrow">О профиле</p>
			<h2>${AppShared.escapeHtml(appState.user.displayName)}</h2>
			<p class="muted">${AppShared.escapeHtml(appState.profile.bio || "Добавь немного информации о себе.")}</p>
		</div>
		<div class="profile-badges">
			<span class="profile-tag"><strong>Возраст:</strong>&nbsp;${AppShared.escapeHtml(String(appState.user.age))}</span>
			<span class="profile-tag"><strong>Город:</strong>&nbsp;${AppShared.escapeHtml(appState.profile.city)}</span>
			<span class="profile-tag"><strong>Статус:</strong>&nbsp;${AppShared.escapeHtml(appState.profile.state)}</span>
		</div>
		<div class="profile-section">
			<p class="section-label">Интересы</p>
			<div class="profile-badges">
				${appState.profile.interests.map(interest => `<span class="profile-tag">${AppShared.escapeHtml(interest)}</span>`).join("")}
			</div>
		</div>
	`;

	appElements.profileAge.value = appState.user.age ?? "";
	appElements.profileCity.value = appState.profile.city || "";
	appElements.profileState.value = appState.profile.state || "DEEP_SEARCH";
	appElements.profileBio.value = appState.profile.bio || "";
	appElements.profileLatitude.value = appState.profile.latitude ?? "";
	appElements.profileLongitude.value = appState.profile.longitude ?? "";
	renderInterestGroups(appElements.profileInterestGroups, appState.interestCategories, appState.profile.interests || []);
}

function renderInterestGroups(container, categories, selectedValues) {
	container.innerHTML = categories.map(category => `
		<section class="interest-group">
			<p class="section-label">${AppShared.escapeHtml(category.label)}</p>
			<div class="interest-options">
				${category.options.map(option => `
					<label class="interest-option">
						<input type="checkbox" value="${AppShared.escapeHtml(option)}" ${selectedValues.includes(option) ? "checked" : ""}>
						<span>${AppShared.escapeHtml(option)}</span>
					</label>
				`).join("")}
			</div>
		</section>
	`).join("");
}

function collectCheckedValues(container) {
	return Array.from(container.querySelectorAll("input[type='checkbox']:checked")).map(input => input.value);
}

function avatarMarkup(name, photoUrl, sizeClass = "") {
	if (photoUrl) {
		return `<div class="avatar ${sizeClass}"><img alt="${AppShared.escapeHtml(name)}" src="${photoUrl}"></div>`;
	}
	return `<div class="avatar ${sizeClass}">${AppShared.escapeHtml(AppShared.initialsFrom(name))}</div>`;
}

function matchPhotoMarkup(match) {
	if (match.photoUrl) {
		return `<div class="match-photo"><img alt="${AppShared.escapeHtml(match.displayName)}" src="${match.photoUrl}"></div>`;
	}
	return `<div class="match-photo fallback">${AppShared.escapeHtml(AppShared.initialsFrom(match.displayName))}</div>`;
}

function previewMessage(message) {
	const value = (message || "").trim();
	if (!value) {
		return "Без сообщений";
	}
	return value.length > 32 ? value.slice(0, 32) + "..." : value;
}

function autoResizeComposer(reset = false) {
	if (reset) {
		appElements.chatMessage.style.height = "46px";
		return;
	}

	appElements.chatMessage.style.height = "46px";
	appElements.chatMessage.style.height = Math.min(appElements.chatMessage.scrollHeight, 140) + "px";
}

function emptyState(message) {
	return `<div class="empty-state">${AppShared.escapeHtml(message)}</div>`;
}

function showNotice(message, tone) {
	if (!appElements.notice) {
		if (tone === "error") {
			showFloatingToast(message);
		}
		return;
	}
	appElements.notice.textContent = message;
	appElements.notice.classList.remove("is-success", "is-error");
	if (tone === "success") {
		appElements.notice.classList.add("is-success");
	}
	if (tone === "error") {
		appElements.notice.classList.add("is-error");
	}
}

function showFloatingToast(message) {
	const existing = document.querySelector(".floating-toast");
	if (existing) {
		existing.remove();
	}

	const toast = document.createElement("div");
	toast.className = "floating-toast";
	toast.textContent = message;
	document.body.append(toast);
	window.setTimeout(() => {
		toast.remove();
	}, 3200);
}

function logout() {
	AppShared.clearAuth();
	window.location.href = "/";
}

function handleAuthFailure(error) {
	AppShared.clearAuth();
	showNotice(error.message, "error");
	window.location.href = "/auth.html?mode=login";
}
