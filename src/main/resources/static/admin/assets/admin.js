(function () {
  'use strict';

  var COLLAPSED_GROUPS_KEY = 'admin-sidebar-collapsed-groups';
  var RAIL_KEY = 'admin-sidebar-rail';
  var THEME_KEY = 'admin-theme';
  var TOAST_VISIBLE_MILLISECONDS = 4000;

  // 저장소 접근이 막힌 환경(사생활 보호 모드 등)에서도 화면이 깨지지 않도록 모든 접근을 감싼다.
  function readStorage(key, fallback) {
    try {
      var value = JSON.parse(localStorage.getItem(key));
      return value === null ? fallback : value;
    } catch (error) {
      return fallback;
    }
  }

  function writeStorage(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      // 저장 실패는 기능 저하만 일으키므로 무시한다.
    }
  }

  // 첫 페인트 전에 적용해 라이트/다크 깜빡임을 막는다.
  var storedTheme = readStorage(THEME_KEY, null);
  if (storedTheme === 'light' || storedTheme === 'dark') {
    document.documentElement.setAttribute('data-theme', storedTheme);
  }

  function isDarkTheme() {
    var explicitTheme = document.documentElement.getAttribute('data-theme');
    if (explicitTheme) {
      return explicitTheme === 'dark';
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  function toggleTheme() {
    var nextTheme = isDarkTheme() ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', nextTheme);
    writeStorage(THEME_KEY, nextTheme);
  }

  /* 사이드바 */
  function initSidebar() {
    var app = document.querySelector('.app');
    var side = document.getElementById('side');
    if (!app || !side) {
      return;
    }
    var collapsedGroups = readStorage(COLLAPSED_GROUPS_KEY, []);
    app.classList.toggle('rail', readStorage(RAIL_KEY, false) === true);

    side.querySelectorAll('.sidebar-group').forEach(function (groupButton) {
      var shut = collapsedGroups.indexOf(groupButton.dataset.group) > -1;
      setGroupShut(groupButton, shut);
    });

    side.addEventListener('click', function (event) {
      var groupButton = event.target.closest('.sidebar-group');
      if (groupButton) {
        var willShut = !groupButton.classList.contains('shut');
        setGroupShut(groupButton, willShut);
        var shutGroups = Array.prototype.map.call(side.querySelectorAll('.sidebar-group.shut'), function (button) {
          return button.dataset.group;
        });
        writeStorage(COLLAPSED_GROUPS_KEY, shutGroups);
        return;
      }
      if (event.target.closest('#railbtn')) {
        var rail = !app.classList.contains('rail');
        app.classList.toggle('rail', rail);
        writeStorage(RAIL_KEY, rail);
      }
    });

    var searchInput = document.getElementById('sidebar-search');
    if (searchInput) {
      searchInput.addEventListener('input', function () {
        filterMenu(side, searchInput.value.trim());
      });
    }
  }

  function setGroupShut(groupButton, shut) {
    groupButton.classList.toggle('shut', shut);
    groupButton.setAttribute('aria-expanded', String(!shut));
    var items = groupButton.nextElementSibling;
    if (items) {
      items.classList.toggle('shut', shut);
    }
  }

  function filterMenu(side, query) {
    side.querySelectorAll('.sidebar-group').forEach(function (groupButton) {
      var items = groupButton.nextElementSibling;
      var visibleCount = 0;
      items.querySelectorAll('.nav').forEach(function (navLink) {
        var matches = !query || navLink.textContent.indexOf(query) > -1;
        navLink.hidden = !matches;
        if (matches) {
          visibleCount += 1;
        }
      });
      groupButton.hidden = visibleCount === 0;
      items.hidden = visibleCount === 0;
      // 검색 중에는 접힌 그룹도 결과를 보여 준다.
      items.classList.toggle('shut', !query && groupButton.classList.contains('shut'));
    });
    side.querySelectorAll(':scope > .nav').forEach(function (navLink) {
      navLink.hidden = Boolean(query) && navLink.textContent.indexOf(query) === -1;
    });
  }

  /* 확인 모달 */
  var confirmedForms = new WeakSet();
  var modalReturnFocus = null;

  function closeModal() {
    var layer = document.getElementById('layer');
    if (layer) {
      layer.innerHTML = '';
    }
    if (modalReturnFocus) {
      modalReturnFocus.focus();
      modalReturnFocus = null;
    }
  }

  function openConfirmModal(message, onConfirm) {
    var layer = document.getElementById('layer');
    if (!layer) {
      onConfirm();
      return;
    }
    modalReturnFocus = document.activeElement;
    layer.innerHTML = '';
    var scrim = document.createElement('div');
    scrim.className = 'scrim';
    scrim.setAttribute('role', 'dialog');
    scrim.setAttribute('aria-modal', 'true');
    var modal = document.createElement('div');
    modal.className = 'modal';
    var title = document.createElement('h3');
    title.textContent = '확인';
    var body = document.createElement('div');
    body.className = 'subtitle';
    body.textContent = message;
    var actions = document.createElement('div');
    actions.className = 'actions';
    var cancelButton = document.createElement('button');
    cancelButton.type = 'button';
    cancelButton.className = 'btn';
    cancelButton.textContent = '취소';
    var okButton = document.createElement('button');
    okButton.type = 'button';
    okButton.className = 'btn primary';
    okButton.textContent = '확인';
    actions.append(cancelButton, okButton);
    modal.append(title, body, actions);
    scrim.append(modal);
    layer.append(scrim);
    cancelButton.focus();
    cancelButton.addEventListener('click', closeModal);
    okButton.addEventListener('click', function () {
      closeModal();
      onConfirm();
    });
    scrim.addEventListener('click', function (event) {
      if (event.target === scrim) {
        closeModal();
      }
    });
  }

  document.addEventListener('submit', function (event) {
    var form = event.target;
    if (confirmedForms.has(form)) {
      confirmedForms.delete(form);
      return;
    }
    var submitter = event.submitter;
    var message = (submitter && submitter.dataset.confirm) || form.dataset.confirm;
    if (!message) {
      return;
    }
    event.preventDefault();
    event.stopImmediatePropagation();
    openConfirmModal(message, function () {
      confirmedForms.add(form);
      form.requestSubmit(submitter || undefined);
    });
  }, true);

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      closeModal();
    }
  });

  /* 클릭 위임: 모바일 메뉴, 테마 */
  document.addEventListener('click', function (event) {
    var menuButton = event.target.closest('#menu');
    if (menuButton) {
      var side = document.getElementById('side');
      var scrim = document.getElementById('sidescrim');
      var open = side.classList.toggle('open');
      if (scrim) {
        scrim.classList.toggle('open', open);
      }
      return;
    }
    if (event.target.closest('#sidescrim')) {
      document.getElementById('side').classList.remove('open');
      document.getElementById('sidescrim').classList.remove('open');
      return;
    }
    if (event.target.closest('#theme')) {
      toggleTheme();
    }
  });

  /* 일괄 처리 바: 선택이 있을 때만 보인다. JS가 없으면 바가 항상 보여 폼은 그대로 동작한다. */
  function updateBulkBars() {
    document.querySelectorAll('[data-bulk-form]').forEach(function (form) {
      var rows = form.querySelectorAll('[data-bulk-row]:not(:disabled)');
      var checkedCount = form.querySelectorAll('[data-bulk-row]:checked').length;
      var bar = form.querySelector('[data-bulk-bar]');
      var allCheckbox = form.querySelector('[data-bulk-all]');
      bar.hidden = checkedCount === 0;
      form.querySelector('[data-bulk-count]').textContent = String(checkedCount);
      if (allCheckbox) {
        allCheckbox.checked = rows.length > 0 && checkedCount === rows.length;
      }
      form.querySelectorAll('[data-bulk-row]').forEach(function (checkbox) {
        var tableRow = checkbox.closest('tr');
        if (tableRow) {
          tableRow.classList.toggle('selected', checkbox.checked);
        }
      });
    });
  }

  document.addEventListener('change', function (event) {
    var target = event.target;
    if (target.matches('[data-bulk-all]')) {
      target.closest('[data-bulk-form]').querySelectorAll('[data-bulk-row]:not(:disabled)').forEach(function (checkbox) {
        checkbox.checked = target.checked;
      });
      updateBulkBars();
    } else if (target.matches('[data-bulk-row]')) {
      updateBulkBars();
    }
  });

  document.addEventListener('htmx:afterSettle', updateBulkBars);

  /* toast */
  function initToasts() {
    document.querySelectorAll('.toast').forEach(function (toast) {
      setTimeout(function () {
        toast.classList.add('hide');
        setTimeout(function () {
          toast.remove();
        }, 400);
      }, TOAST_VISIBLE_MILLISECONDS);
    });
  }

  /* HTMX */
  document.addEventListener('htmx:configRequest', function (event) {
    var token = document.querySelector('meta[name="_csrf"]');
    var header = document.querySelector('meta[name="_csrf_header"]');
    if (token && header) {
      event.detail.headers[header.content] = token.content;
    }
  });

  // 부분 응답 자리에 페이지 전체(만료된 세션의 로그인 페이지, 예외 시의 오류 페이지)가 들어오면 레이아웃이 중첩되므로, 끼워 넣지 않고 전체 새로고침한다.
  document.addEventListener('htmx:beforeSwap', function (event) {
    var xhr = event.detail.xhr;
    var redirectedToLogin = xhr && xhr.responseURL && xhr.responseURL.indexOf('/admin/login') > -1;
    var responseText = xhr && typeof xhr.responseText === 'string' ? xhr.responseText : '';
    var containsWholePage = responseText.indexOf('data-admin-login') > -1 || responseText.indexOf('data-admin-layout') > -1;
    if (redirectedToLogin || containsWholePage) {
      event.detail.shouldSwap = false;
      // GET 요청이면 같은 주소를 전체 페이지로 다시 열어 사용자가 실제 오류 화면을 보게 한다. 그 외(POST 등)는 재전송을 피하려고 새로고침만 한다.
      var isGetRequest = event.detail.requestConfig && event.detail.requestConfig.verb === 'get';
      if (!redirectedToLogin && isGetRequest && xhr.responseURL) {
        window.location.assign(xhr.responseURL);
      } else {
        window.location.reload();
      }
    }
  });

  document.addEventListener('DOMContentLoaded', function () {
    initSidebar();
    initToasts();
    updateBulkBars();
  });
})();
