const AppShared = window.SynxoShared;

const appState = {
	auth: AppShared.loadAuth(),
	user: null,
	profile: null,
	matches: [],
	dailyMatch: null,
	achievements: [],
	chats: [],
	conversation: [],
	interestCategories: [],
	activeView: "home",
	strategy: "RECOMMENDATION",
	selectedChatUserId: null,
	visibleNotificationIds: new Set(),
	dismissedNotificationIds: new Set(),
	notificationPollId: null
};

const appElements = {
	sessionCopy: document.getElementById("session-copy"),
	refreshButton: document.getElementById("refresh-button"),
	logoutButton: document.getElementById("logout-button"),
	tabButtons: Array.from(document.querySelectorAll("[data-view]")),
	views: Array.from(document.querySelectorAll(".view-section")),
	strategyButtons: Array.from(document.querySelectorAll("[data-strategy]")),
	discoverEyebrow: document.getElementById("discover-eyebrow"),
	discoverTitle: document.getElementById("discover-title"),
	homeHero: document.getElementById("home-hero"),
	homeStats: document.getElementById("home-stats"),
	dailyMatchPanel: document.getElementById("daily-match-panel"),
	achievementList: document.getElementById("achievement-list"),
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
	profileLocationStatus: document.getElementById("profile-location-status"),
	detectLocationButton: document.getElementById("detect-location-button"),
	disableLocationButton: document.getElementById("disable-location-button"),
	matchingPreferencesEnabled: document.getElementById("matching-preferences-enabled"),
	interestPriority: document.getElementById("interest-priority"),
	distancePriority: document.getElementById("distance-priority"),
	intentionPriority: document.getElementById("intention-priority"),
	activityPriority: document.getElementById("activity-priority"),
	socialPriority: document.getElementById("social-priority"),
	interestPriorityValue: document.getElementById("interest-priority-value"),
	distancePriorityValue: document.getElementById("distance-priority-value"),
	intentionPriorityValue: document.getElementById("intention-priority-value"),
	activityPriorityValue: document.getElementById("activity-priority-value"),
	socialPriorityValue: document.getElementById("social-priority-value"),
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
		await loadNotifications();
		startNotificationPolling();
	} catch (error) {
		handleAppLoadError(error);
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
			renderDiscoverHeader();
			await Promise.all([loadMatches(), loadDailyMatch()]);
			renderDiscover();
			renderHome();
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

	appElements.detectLocationButton.addEventListener("click", async () => {
		await detectPreciseLocation();
	});

	appElements.disableLocationButton.addEventListener("click", async () => {
		await disablePreciseLocation();
	});

	[
		[appElements.interestPriority, appElements.interestPriorityValue],
		[appElements.distancePriority, appElements.distancePriorityValue],
		[appElements.intentionPriority, appElements.intentionPriorityValue],
		[appElements.activityPriority, appElements.activityPriorityValue],
		[appElements.socialPriority, appElements.socialPriorityValue]
	].forEach(([input, valueNode]) => {
		input.addEventListener("input", () => {
			valueNode.textContent = input.value;
		});
	});

	appElements.matchingPreferencesEnabled.addEventListener("change", () => {
		renderMatchingPreferenceState();
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

	const secondaryLoads = await Promise.allSettled([
		loadMatches(),
		loadDailyMatch(),
		loadChats(),
		loadAchievements()
	]);

	const failedSecondaryLoad = secondaryLoads.find(result => result.status === "rejected");
	if (failedSecondaryLoad) {
		if (AppShared.isUnauthorizedError(failedSecondaryLoad.reason)) {
			throw failedSecondaryLoad.reason;
		}

		console.error("Non-auth app data load failure:", failedSecondaryLoad.reason);
		showNotice("Часть данных не загрузилась, но вход в аккаунт выполнен.", "error");
	}

	if (appState.selectedChatUserId) {
		await loadConversation(appState.selectedChatUserId);
	}

	renderAll();
	if (showBanner) {
		showNotice("Данные обновлены.", "success");
	}
}

async function loadMatches() {
	const matches = await AppShared.apiRequest("/api/matches?strategy=" + encodeURIComponent(appState.strategy));
	appState.matches = sortMatchesForCurrentStrategy(matches);
}

async function loadDailyMatch() {
	appState.dailyMatch = await AppShared.apiRequest("/api/matches/daily?strategy=" + encodeURIComponent(appState.strategy));
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

		await AppShared.apiRequest("/api/profiles/me/matching-preferences", {
			method: "PUT",
			body: {
				enabled: appElements.matchingPreferencesEnabled.checked,
				interestPriority: Number(appElements.interestPriority.value),
				distancePriority: Number(appElements.distancePriority.value),
				intentionPriority: Number(appElements.intentionPriority.value),
				activityPriority: Number(appElements.activityPriority.value),
				socialPriority: Number(appElements.socialPriority.value)
			}
		});

		await refreshApp(false);
		appState.activeView = "profile";
		renderViews();
		showNotice("Профиль обновлен.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

async function detectPreciseLocation() {
	if (!navigator.geolocation) {
		showNotice("Браузер не поддерживает определение геолокации.", "error");
		return;
	}

	try {
		appElements.detectLocationButton.disabled = true;
		const position = await readBrowserLocation();
		await AppShared.apiRequest("/api/profiles/me/precise-location", {
			method: "PUT",
			body: {
				enabled: true,
				latitude: position.coords.latitude,
				longitude: position.coords.longitude
			}
		});
		await refreshApp(false);
		showNotice("Точная локация включена.", "success");
	} catch (error) {
		showNotice(error.message || "Не удалось определить точную локацию.", "error");
	} finally {
		appElements.detectLocationButton.disabled = false;
	}
}

async function disablePreciseLocation() {
	try {
		await AppShared.apiRequest("/api/profiles/me/precise-location", {
			method: "PUT",
			body: { enabled: false }
		});
		await refreshApp(false);
		showNotice("Точная локация отключена. Используем координаты города.", "success");
	} catch (error) {
		showNotice(error.message, "error");
	}
}

function readBrowserLocation() {
	return new Promise((resolve, reject) => {
		navigator.geolocation.getCurrentPosition(resolve, error => {
			const fallbackMessage = "Браузер не дал доступ к геолокации.";
			reject(new Error(error.message || fallbackMessage));
		}, {
			enableHighAccuracy: true,
			timeout: 10000,
			maximumAge: 60000
		});
	});
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
	renderDiscoverHeader();
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

function renderDiscoverHeader() {
	if (!appElements.discoverTitle || !appElements.discoverEyebrow) {
		return;
	}

	if (appState.strategy === "PROXIMITY") {
		appElements.discoverEyebrow.textContent = "Лента по близости";
		appElements.discoverTitle.textContent = "Люди рядом с тобой";
		return;
	}

	appElements.discoverEyebrow.textContent = "Лента по интересам";
	appElements.discoverTitle.textContent = "Люди с общими интересами";
}

function renderHome() {
	const pendingLikes = appState.matches.filter(match => match.likedYou && !match.likedByYou).length;
	const mutualLikes = appState.matches.filter(match => match.mutualLike).length;
	const personalizedCopy = appState.profile?.matchingPreferences?.enabled
		? "Лента учитывает твои персональные веса совместимости."
		: "Лента работает по прозрачной формуле совместимости.";

	appElements.homeHero.innerHTML = `
		<p class="eyebrow">Текущая сессия</p>
		<h1>${AppShared.escapeHtml(appState.user.displayName)}, здесь видны люди, с которыми у тебя уже есть общие интересы.</h1>
		<p class="hero-text">Сначала отметь понравившийся профиль в ленте. Если симпатия взаимная, человек появится в чатах, и можно будет начать полноценную переписку. ${AppShared.escapeHtml(personalizedCopy)}</p>
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

	renderDailyMatch();
	renderAchievements();
}

function renderDailyMatch() {
	const payload = appState.dailyMatch;
	const match = payload && payload.match ? payload.match : null;

	if (!match) {
		appElements.dailyMatchPanel.innerHTML = `
			<div class="section-header tight">
				<div>
					<p class="eyebrow">Daily Match</p>
					<h2>Кандидат дня</h2>
				</div>
			</div>
			${emptyState("Сегодня система не нашла достаточно релевантного кандидата. Попробуй обновить интересы или зайти позже.")}
		`;
		return;
	}

	appElements.dailyMatchPanel.innerHTML = `
		<div class="section-header tight">
			<div>
				<p class="eyebrow">Daily Match</p>
				<h2>Кандидат дня</h2>
			</div>
			<span class="profile-tag">${AppShared.escapeHtml(String(payload.generatedFor))}</span>
		</div>
		<div class="daily-match-layout">
			${matchPhotoMarkup(match)}
			<div class="daily-match-copy">
				<div class="match-copy-top">
					<div>
						<h3>${AppShared.escapeHtml(match.displayName)}</h3>
						<p class="muted">${AppShared.escapeHtml(match.city)} · ${AppShared.escapeHtml(String(match.age))} лет</p>
					</div>
					<div class="score-stack">
						<strong>${AppShared.escapeHtml(Math.round(match.score || 0) + "%")}</strong>
						<span>${match.distanceKm == null ? "n/a" : AppShared.escapeHtml(match.distanceKm.toFixed(1) + " km")}</span>
					</div>
				</div>
				<div class="reason-list">
					${(match.whyMatched || []).map(reason => `<span class="reason-chip">${AppShared.escapeHtml(reason)}</span>`).join("")}
				</div>
				<div class="form-actions">
					${renderMatchAction(match)}
				</div>
			</div>
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
						<div class="score-stack">
							<strong>${AppShared.escapeHtml(Math.round(match.score || 0) + "%")}</strong>
							<span>${match.distanceKm == null ? "n/a" : AppShared.escapeHtml(match.distanceKm.toFixed(1) + " km")}</span>
						</div>
					</div>

					<div>
						<p class="section-label">Общие интересы</p>
						<div class="chip-row">${match.sharedInterests.map(interest => `<span class="chip">${AppShared.escapeHtml(interest)}</span>`).join("")}</div>
					</div>

					<div>
						<p class="section-label">Почему вы совпали</p>
						<div class="reason-list">${(match.whyMatched || []).map(reason => `<span class="reason-chip">${AppShared.escapeHtml(reason)}</span>`).join("")}</div>
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
	const matchingPreferences = appState.profile.matchingPreferences || defaultMatchingPreferences();

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
			<span class="profile-tag"><strong>Стрик:</strong>&nbsp;${AppShared.escapeHtml(String(appState.profile.activityStreakDays || 0))} дн.</span>
		</div>
		<div class="profile-section">
			<p class="section-label">Интересы</p>
			<div class="profile-badges">
				${appState.profile.interests.map(interest => `<span class="profile-tag">${AppShared.escapeHtml(interest)}</span>`).join("")}
			</div>
		</div>
		<div class="profile-section">
			<p class="section-label">Открытые достижения</p>
			<div class="reason-list">
				${appState.achievements.length
					? appState.achievements.map(achievement => `<span class="reason-chip">${AppShared.escapeHtml(achievement.title)}</span>`).join("")
					: '<span class="muted">Пока нет открытых достижений.</span>'}
			</div>
		</div>
	`;

	appElements.profileAge.value = appState.user.age ?? "";
	appElements.profileCity.value = appState.profile.city || "";
	appElements.profileState.value = appState.profile.state || "DEEP_SEARCH";
	appElements.profileBio.value = appState.profile.bio || "";
	appElements.profileLocationStatus.textContent = locationStatusText(appState.profile);
	appElements.disableLocationButton.disabled = !appState.profile.preciseLocationEnabled;
	appElements.matchingPreferencesEnabled.checked = Boolean(matchingPreferences.enabled);
	appElements.interestPriority.value = matchingPreferences.interestPriority ?? 100;
	appElements.distancePriority.value = matchingPreferences.distancePriority ?? 100;
	appElements.intentionPriority.value = matchingPreferences.intentionPriority ?? 100;
	appElements.activityPriority.value = matchingPreferences.activityPriority ?? 100;
	appElements.socialPriority.value = matchingPreferences.socialPriority ?? 100;
	syncPriorityLabels();
	renderMatchingPreferenceState();
	renderInterestGroups(appElements.profileInterestGroups, appState.interestCategories, appState.profile.interests || []);
}

function renderAchievements() {
	if (!appElements.achievementList) {
		return;
	}

	if (!appState.achievements.length) {
		appElements.achievementList.innerHTML = emptyState("Пока нет открытых бейджей. Они появятся после первых заметных шагов в приложении.");
		return;
	}

	appElements.achievementList.innerHTML = appState.achievements.map(achievement => `
		<article class="achievement-item">
			<div class="achievement-badge">${AppShared.escapeHtml(achievement.title.charAt(0))}</div>
			<div class="achievement-copy">
				<strong>${AppShared.escapeHtml(achievement.title)}</strong>
				<p>${AppShared.escapeHtml(achievement.description)}</p>
				<time>${AppShared.escapeHtml(AppShared.formatDate(achievement.unlockedAt))}</time>
			</div>
		</article>
	`).join("");
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

function sortMatchesForCurrentStrategy(matches) {
	const normalized = Array.isArray(matches) ? [...matches] : [];
	if (appState.strategy === "PROXIMITY") {
		return normalized.sort((left, right) => {
			const leftDistance = left.distanceKm == null ? Number.MAX_SAFE_INTEGER : Number(left.distanceKm);
			const rightDistance = right.distanceKm == null ? Number.MAX_SAFE_INTEGER : Number(right.distanceKm);
			if (leftDistance !== rightDistance) {
				return leftDistance - rightDistance;
			}
			return Number(right.score || 0) - Number(left.score || 0);
		});
	}

	return normalized.sort((left, right) => {
		const leftShared = Array.isArray(left.sharedInterests) ? left.sharedInterests.length : 0;
		const rightShared = Array.isArray(right.sharedInterests) ? right.sharedInterests.length : 0;
		if (leftShared !== rightShared) {
			return rightShared - leftShared;
		}
		return Number(right.score || 0) - Number(left.score || 0);
	});
}

function defaultMatchingPreferences() {
	return {
		enabled: false,
		interestPriority: 100,
		distancePriority: 100,
		intentionPriority: 100,
		activityPriority: 100,
		socialPriority: 100
	};
}

function syncPriorityLabels() {
	appElements.interestPriorityValue.textContent = appElements.interestPriority.value;
	appElements.distancePriorityValue.textContent = appElements.distancePriority.value;
	appElements.intentionPriorityValue.textContent = appElements.intentionPriority.value;
	appElements.activityPriorityValue.textContent = appElements.activityPriority.value;
	appElements.socialPriorityValue.textContent = appElements.socialPriority.value;
}

function renderMatchingPreferenceState() {
	const enabled = appElements.matchingPreferencesEnabled.checked;
	[
		appElements.interestPriority,
		appElements.distancePriority,
		appElements.intentionPriority,
		appElements.activityPriority,
		appElements.socialPriority
	].forEach(input => {
		input.disabled = !enabled;
	});
}

function locationStatusText(profile) {
	const mode = profile.preciseLocationEnabled
		? "Точная локация включена"
		: "Используются координаты города";
	const cityCoordinates = coordinatesLabel(profile.cityLatitude, profile.cityLongitude);
	const activeCoordinates = coordinatesLabel(profile.latitude, profile.longitude);
	return `${mode}. Город: ${profile.city} (${cityCoordinates}). Для мэтчей: ${activeCoordinates}.`;
}

function coordinatesLabel(latitude, longitude) {
	if (latitude == null || longitude == null) {
		return "n/a";
	}
	return Number(latitude).toFixed(4) + ", " + Number(longitude).toFixed(4);
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
	showFloatingToast(message, tone);
}

function showFloatingToast(message, tone = "info") {
	const stack = notificationStack();
	const toast = document.createElement("article");
	toast.className = "notification-toast app-toast";
	if (tone === "success") {
		toast.classList.add("is-success");
	}
	if (tone === "error") {
		toast.classList.add("is-error");
	}
	toast.innerHTML = `
		<div>
			<p class="section-label">${AppShared.escapeHtml(tone === "success" ? "Успешно" : tone === "error" ? "Ошибка" : "Информация")}</p>
			<p>${AppShared.escapeHtml(message)}</p>
		</div>
		<button class="toast-close" aria-label="Скрыть уведомление" type="button">×</button>
	`;

	const dismiss = () => {
		if (toast.isConnected) {
			toast.remove();
		}
	};
	toast.querySelector(".toast-close").addEventListener("click", dismiss);
	stack.append(toast);
	window.setTimeout(dismiss, 4200);
}

async function loadNotifications() {
	try {
		const notifications = await AppShared.apiRequest("/api/notifications");
		notifications.forEach(showNotificationToast);
	} catch (error) {
		if (AppShared.isUnauthorizedError(error)) {
			handleAuthFailure(error);
		}
	}
}

async function loadAchievements() {
	appState.achievements = await AppShared.apiRequest("/api/achievements");
}

function startNotificationPolling() {
	if (appState.notificationPollId) {
		window.clearInterval(appState.notificationPollId);
	}
	appState.notificationPollId = window.setInterval(loadNotifications, 8000);
}

function showNotificationToast(notification) {
	if (
		appState.visibleNotificationIds.has(notification.id)
		|| appState.dismissedNotificationIds.has(notification.id)
	) {
		return;
	}

	appState.visibleNotificationIds.add(notification.id);
	const stack = notificationStack();
	const toast = document.createElement("article");
	toast.className = "notification-toast";
	toast.dataset.notificationId = String(notification.id);
	toast.innerHTML = `
		<div>
			<p class="section-label">${AppShared.escapeHtml(notification.type)}</p>
			<p>${AppShared.escapeHtml(notification.message)}</p>
			<time>${AppShared.escapeHtml(AppShared.formatDate(notification.createdAt))}</time>
		</div>
		<button class="toast-close" aria-label="Скрыть уведомление" type="button">×</button>
	`;

	const dismiss = () => dismissNotification(notification.id, toast);
	toast.querySelector(".toast-close").addEventListener("click", dismiss);
	stack.append(toast);
	window.setTimeout(dismiss, 7000);
}

function notificationStack() {
	let stack = document.querySelector(".notification-stack");
	if (!stack) {
		stack = document.createElement("div");
		stack.className = "notification-stack";
		document.body.append(stack);
	}
	return stack;
}

async function dismissNotification(notificationId, toast) {
	if (appState.dismissedNotificationIds.has(notificationId)) {
		return;
	}

	appState.dismissedNotificationIds.add(notificationId);
	appState.visibleNotificationIds.delete(notificationId);
	if (toast && toast.isConnected) {
		toast.remove();
	}

	try {
		await AppShared.apiRequest("/api/notifications/" + notificationId + "/dismiss", { method: "PATCH" });
	} catch (error) {
		appState.dismissedNotificationIds.delete(notificationId);
	}
}

function logout() {
	AppShared.clearAuth();
	window.location.href = "/";
}

function handleAppLoadError(error) {
	if (AppShared.isUnauthorizedError(error)) {
		handleAuthFailure(error);
		return;
	}

	console.error("App load error:", error);
	showNotice(error.message || "Не удалось загрузить приложение после входа.", "error");
}

function handleAuthFailure(error) {
	AppShared.clearAuth();
	console.error("Authentication error:", error);
	window.location.href = "/auth.html?mode=login";
}
