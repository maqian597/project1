(function() {

	/**
	 * Variables
	 */
	var user_id = '1111';
	var lng = -122.08;
	var lat = 37.38;

	/**
	 * Initialize
	 */
	function init() {
//		// Register event listeners
		$('login-btn').addEventListener('click', loadUser);
		$('nearby-btn').addEventListener('click', loadNearbyItems);
		$('fav-btn').addEventListener('click', loadFavoriteItems);
		$('recommend-btn').addEventListener('click', loadRecommendedItems);
		initGeoLocation();
		
		//onSessionValid({'user_id': 1111, 'name': 'John Smith'});
		onSessionInvalid();	
		//checkLogIn();
	}

	function initGeoLocation() {
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(onPositionUpdated,
					onLoadPositionFailed, {
						maximumAge : 60000
					});
			showLoadingMessage('Retrieving your location...');
		} else {
			onLoadPositionFailed();
		}
	}

	function onPositionUpdated(position) {
		lat = position.coords.latitude;
		lng = position.coords.longitude;

		loadNearbyItems();
	}

	function onLoadPositionFailed() {
		console.warn('navigator.geolocation is not available');
		getLocationFromIP();
	}

	function getLocationFromIP() {
		// Get location from http://ipinfo.io/json
		var url = 'http://ipinfo.io/json'
		var req = null;
		ajax('GET', url, req, function(res) {
			var result = JSON.parse(res);
			if ('loc' in result) {
				var loc = result.loc.split(',');
				lat = loc[0];
				lng = loc[1];
			} else {
				console.warn('Getting location by IP failed.');
			}
			loadNearbyItems();
		});					
	}
	
	//this is check session or login function
	function checkLogIn() {
		// The request parameters
		var url = './login';
		var req = JSON.stringify({});
		ajax('GET', url, req, function(res) {
			var result = JSON.parse(res);
			// Register event listeners
			console.log(res)
			var side = $('#sidebar_item');
			side.innerHTML ='';
			$('#logout_btn').hide();
			$('nearby-btn').addEventListener('click', loadNearbyItems);
			$('fav-btn').addEventListener('click', loadFavoriteItems);
			$('recommend-btn').addEventListener('click', loadRecommendedItems);
			initGeoLocation();
		},
		// failed callback
		function() {
			showErrorMessage('Cannot load nearby items.');
		});
	}
	
	//this is for the session valid;
	function onSessionValid() {
		
	}
	// -----------------------------------
	// Helper Functions
	// -----------------------------------

	/**
	 * A helper function that makes a navigation button active
	 * 
	 * @param btnId -
	 *            The id of the navigation button
	 */
	function activeBtn(btnId) {
		var btns = document.getElementsByClassName('main-nav-btn');

		// deactivate all navigation buttons
		for (var i = 0; i < btns.length; i++) {
			btns[i].className = btns[i].className.replace(/\bactive\b/, '');
		}

		// active the one that has id = btnId
		var btn = $(btnId);
		btn.className += ' active';
	}

	function showLoadingMessage(msg) {
		var itemList = $('item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> '
				+ msg + '</p>';
	}

	function showWarningMessage(msg) {
		var itemList = $('item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> '
				+ msg + '</p>';
	}

	function showErrorMessage(msg) {
		var itemList = $('item-list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> '
				+ msg + '</p>';
	}

	/**
	 * A helper function that creates a DOM element <tag options...>
	 * 
	 * @param tag
	 * @param options
	 * @returns
	 */
	function $(tag, options) {
		if (!options) {
			return document.getElementById(tag);
		}

		var element = document.createElement(tag);

		for ( var option in options) {
			if (options.hasOwnProperty(option)) {
				element[option] = options[option];
			}
		}

		return element;
	}

	/**
	 * AJAX helper
	 * 
	 * @param method -
	 *            GET|POST|PUT|DELETE
	 * @param url -
	 *            API end point
	 * @param callback -
	 *            This the successful callback
	 * @param errorHandler -
	 *            This is the failed callback
	 */
	function ajax(method, url, data, callback, errorHandler) {
		var xhr = new XMLHttpRequest();

		xhr.open(method, url, true);

		xhr.onload = function() {
			switch (xhr.status) {
			case 200:
				callback(xhr.responseText);
				break;
			case 403:
				onSessionInvalid();
				break;
			case 401:
				errorHandler();
				break;
			}
		};
		console.log(xhr);
		xhr.onerror = function() {
			console.error("The request couldn't be completed.");
			errorHandler();
		};

		if (data === null) {
			xhr.send();
		} else {
			xhr.setRequestHeader("Content-Type",
					"application/json;charset=utf-8");
			xhr.send(data);
		}
	}
	function onSessionInvalid() {
		console.log("session invalid, so go to the log in form")
		var login_form = $('login_form')
		var eventRecom = $('sidebaritem')
		login_form.style.display = 'block';
		eventRecom.style.display = 'none';
	}

	// -------------------------------------
	// AJAX call server-side APIs
	// -------------------------------------

	/**
	 * API #1 Load the nearby items API end point: [GET]
	 * /Titan/search?user_id=1111&lat=37.38&lon=-122.08
	 */
	function loadNearbyItems() {
		console.log('loadNearbyItems');
		activeBtn('nearby-btn');

		// The request parameters
		var url = './search';
		var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Loading nearby items...');

		// make AJAX call
		ajax('GET', url + '?' + params, req,
		// successful callback
		function(res) {
			var items = JSON.parse(res);
			if (!items || items.length === 0) {
				showWarningMessage('No nearby item.');
			} else {
				listItems(items);
			}
		},
		// failed callback
		function() {
			showErrorMessage('Cannot load nearby items.');
		});
	}

	/**
	 * API #2 Load favorite (or visited) items API end point: [GET]
	 * /Titan/history?user_id=1111
	 */
	function loadFavoriteItems() {
		activeBtn('fav-btn');

		// The request parameters
		var url = './history';
		var params = 'user_id=' + user_id;
		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Loading favorite items...');

		// make AJAX call
		ajax('GET', url + '?' + params, req, function(res) {
			var items = JSON.parse(res);
			if (!items || items.length === 0) {
				showWarningMessage('No favorite item.');
			} else {
				listItems(items);
			}
		}, function() {
			showErrorMessage('Cannot load favorite items.');
		});
	}

	/**
	 * API #3 Load recommended items API end point: [GET]
	 * /Titan/recommendation?user_id=1111
	 */
	function loadRecommendedItems() {
		activeBtn('recommend-btn');

		// The request parameters
		var url = './recommend';
		var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;

		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Loading recommended items...');

		// make AJAX call
		ajax(
				'GET',
				url + '?' + params,
				req,
				// successful callback
				function(res) {
					var items = JSON.parse(res);
					if (!items || items.length === 0) {
						showWarningMessage('No recommended item. Make sure you have favorites.');
					} else {
						listItems(items);
					}
				},
				// failed callback
				function() {
					showErrorMessage('Cannot load recommended items.');
				});
	}

	/**
	 * API #4 Toggle favorite (or visited) items
	 * 
	 * @param item_id -
	 *            The item business id
	 * 
	 * API end point: [POST]/[DELETE] /Dashi/history request json data: {
	 * user_id: 1111, visited: [a_list_of_business_ids] }
	 */
	function changeFavoriteItem(item_id) {
		// Check whether this item has been visited or not
		var li = $('item-' + item_id);
		var favIcon = $('fav-icon-' + item_id);
		var favorite = li.dataset.favorite !== 'true';

		// The request parameters
		var url = './history';
		var req = JSON.stringify({
			user_id : user_id,
			favorite : [ item_id ]
		});
		var method = favorite ? 'POST' : 'DELETE';

		ajax(method, url, req,
		// successful callback
		function(res) {
			var result = JSON.parse(res);
			if (result.result === 'SUCCESS') {
				li.dataset.favorite = favorite;
				favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
			}
		});
	}
	/*
	 * API #5 Login/Logout in Recommendation.
	 * /Titan/login
	 * /Titan/logout
	 */
	function loadUser(){
		activeBtn('fav-btn');

		// The request url parameters
		var url = './login';
		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Loading login html...');

		//create the form to log in 
		var side = $('sidebar_item');
		var string = '<section class= "main-section" id="login_form"><p class="center">This is the login page</p><div id="content"><form method="post" id = "submission"><p>UserName/UserId:<input type="text" name="username" id="user_id"> </p><p>Password:<input type="password" name="password" id="pwd"> </p></form><button type="submit" id="login_submit">Login</button></div></section>';
		// make AJAX call
		ajax('GET', url, req, 
		function(res) {
			var items = JSON.parse(res);
			side.innerHTML = string;
			showLoadingMessage(items);
		}, function() {
			showErrorMessage('Cannot load login html.');
		}); 
	}
	// -------------------------------------
	// Create item list
	// -------------------------------------

	/**
	 * List items
	 * 
	 * @param items -
	 *            An array of item JSON objects
	 */
	function listItems(items) {
		// Clear the current results
		var itemList = $('item-list');
		itemList.innerHTML = '';

		for (var i = 0; i < items.length; i++) {
			addItem(itemList, items[i]);
		}
	}

	/**
	 * Add item to the list
	 * 
	 * @param itemList -
	 *            The
	 *            <ul id="item-list">
	 *            tag
	 * @param item -
	 *            The item data (JSON object)
	 */
	function addItem(itemList, item) {
		var item_id = item.item_id;

		// create the <li> tag and specify the id and class attributes
		var li = $('li', {
			id : 'item-' + item_id,
			className : 'item'
		});

		// set the data attribute
		li.dataset.item_id = item_id;
		li.dataset.favorite = item.favorite;

		// item image
		if (item.image_url) {
			li.appendChild($('img', {
				src : item.image_url
			}));
		} else {
			li.appendChild($('img', {
				src : 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
			}))
		}
		// section
		var section = $('div', {});

		// title
		var title = $('a', {
			href : item.url,
			target : '_blank',
			className : 'item-name'
		});
		title.innerHTML = item.name;
		section.appendChild(title);

		// category
		var category = $('p', {
			className : 'item-category'
		});
		category.innerHTML = 'Category: ' + item.categories.join(', ');
		section.appendChild(category);

		var stars = $('div', {
			className : 'stars'
		});
		
		for (var i = 0; i < item.rating; i++) {
			var star = $('i', {
				className : 'fa fa-star'
			});
			stars.appendChild(star);
		}

		if (('' + item.rating).match(/\.5$/)) {
			stars.appendChild($('i', {
				className : 'fa fa-star-half-o'
			}));
		}

		section.appendChild(stars);

		li.appendChild(section);

		// address
		var address = $('p', {
			className : 'item-address'
		});
		//add city to the html
		var addressWithCity = item.address + "<br>" + item.city;
		address.innerHTML = addressWithCity;
		//address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g,'');
		li.appendChild(address);

		// favorite link
		var favLink = $('p', {
			className : 'fav-link'
		});

		favLink.onclick = function() {
			changeFavoriteItem(item_id);
		};

		favLink.appendChild($('i', {
			id : 'fav-icon-' + item_id,
			className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
		}));

		li.appendChild(favLink);

		itemList.appendChild(li);
	}

	init();

})();

// END