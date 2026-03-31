(function () {
    const h = React.createElement;
    const useEffect = React.useEffect;
    const useMemo = React.useMemo;
    const useState = React.useState;
    const useDeferredValue = React.useDeferredValue || (v => v);

    const DEFAULT_CATEGORIES = {
        SOFA: "Phong khach",
        TABLE: "Phong an",
        CHAIR: "Ghe",
        BED: "Phong ngu",
        CABINET: "Luu tru",
        DECOR: "Trang tri"
    };

    const ROOMS = [
        ["Phong khach", "SOFA", "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80"],
        ["Phong an", "TABLE", "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1200&q=80"],
        ["Phong ngu", "BED", "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80"],
        ["Khong gian lam viec", "CHAIR", "https://images.unsplash.com/photo-1519947486511-46149fa0a254?auto=format&fit=crop&w=1200&q=80"]
    ];

    function money(value) {
        return new Intl.NumberFormat("vi-VN").format(Number(value || 0)) + " VND";
    }

    function routeOf(pathname) {
        return pathname.startsWith("/products/")
            ? { page: "detail", slug: decodeURIComponent(pathname.slice("/products/".length)) }
            : { page: "home" };
    }

    async function api(url, options) {
        const res = await fetch(url, { headers: { "Content-Type": "application/json" }, ...options });
        if (!res.ok) {
            throw new Error((await res.text()) || "Khong the tai du lieu");
        }
        return res.status === 204 ? null : res.json();
    }

    function go(path) {
        window.history.pushState({}, "", path);
        window.dispatchEvent(new PopStateEvent("popstate"));
    }

    function App() {
        const [route, setRoute] = useState(routeOf(window.location.pathname));
        const [filters, setFilters] = useState({ keyword: "", category: "", material: "", color: "", sizeLabel: "", minPrice: "", maxPrice: "" });
        const [products, setProducts] = useState([]);
        const [categories, setCategories] = useState([]);
        const [currentUser, setCurrentUser] = useState(null);
        const [addresses, setAddresses] = useState([]);
        const [detail, setDetail] = useState(null);
        const [reviews, setReviews] = useState([]);
        const [reco, setReco] = useState([]);
        const [cart, setCart] = useState({ items: [], totalAmount: 0 });
        const [drawer, setDrawer] = useState(false);
        const [authOpen, setAuthOpen] = useState(false);
        const [chatOpen, setChatOpen] = useState(false);
        const [chatText, setChatText] = useState("");
        const [chatState, setChatState] = useState({ assistantName: "Noi That Assistant", history: [] });
        const [authMode, setAuthMode] = useState("login");
        const [authForm, setAuthForm] = useState({ fullName: "", email: "", password: "", phone: "" });
        const [heroImage, setHeroImage] = useState("");
        const [loading, setLoading] = useState(false);
        const [error, setError] = useState("");
        const deferredKeyword = useDeferredValue(filters.keyword);

        useEffect(function () {
            const onPop = function () { setRoute(routeOf(window.location.pathname)); };
            window.addEventListener("popstate", onPop);
            loadCurrentUser();
            loadCategories();
            return function () { window.removeEventListener("popstate", onPop); };
        }, []);

        useEffect(function () {
            if (currentUser) {
                loadCart();
                loadReco();
                loadAddresses();
            } else {
                setCart({ items: [], totalAmount: 0 });
                setReco([]);
                setAddresses([]);
            }
        }, [currentUser]);

        useEffect(function () {
            if (route.page === "home") {
                loadProducts({ ...filters, keyword: deferredKeyword });
            } else {
                loadDetail(route.slug);
            }
        }, [route, deferredKeyword, filters.category, filters.material, filters.color, filters.sizeLabel, filters.minPrice, filters.maxPrice]);

        async function loadProducts(nextFilters) {
            setLoading(true);
            setError("");
            const params = new URLSearchParams();
            Object.entries(nextFilters).forEach(function (entry) {
                if (String(entry[1] || "").trim()) {
                    params.set(entry[0], entry[1]);
                }
            });
            try {
                setProducts(await api("/api/products" + (params.toString() ? "?" + params.toString() : "")));
            } catch (e) {
                setError(e.message);
            } finally {
                setLoading(false);
            }
        }

        async function loadDetail(slug) {
            setLoading(true);
            setError("");
            try {
                const suffix = currentUser ? "?userId=" + currentUser.id : "";
                const product = await api("/api/products/" + encodeURIComponent(slug) + suffix);
                setDetail(product);
                setHeroImage((product.images || [])[0] || "");
                setReviews(await api("/api/products/" + encodeURIComponent(slug) + "/reviews"));
            } catch (e) {
                setError(e.message);
            } finally {
                setLoading(false);
            }
        }

        async function loadCurrentUser() {
            try {
                setCurrentUser(await api("/api/auth/me"));
            } catch (e) {
                setCurrentUser(null);
            }
        }

        async function loadCart() {
            try {
                setCart(await api("/api/cart/me"));
            } catch (e) {
                console.error(e);
            }
        }

        async function loadCategories() {
            try {
                setCategories(await api("/api/categories"));
            } catch (e) {
                console.error(e);
            }
        }

        async function loadReco() {
            try {
                if (!currentUser) {
                    setReco([]);
                    return;
                }
                setReco(await api("/api/products/recommendations/" + currentUser.id));
            } catch (e) {
                console.error(e);
            }
        }

        async function loadAddresses() {
            try {
                setAddresses(await api("/api/users/me/addresses"));
            } catch (e) {
                console.error(e);
            }
        }

        async function addToCart(productId) {
            if (!currentUser) {
                setAuthMode("login");
                setAuthOpen(true);
                return;
            }
            setCart(await api("/api/cart/me/items", { method: "POST", body: JSON.stringify({ productId: productId, quantity: 1 }) }));
            setDrawer(true);
        }

        async function changeQty(itemId, qty) {
            if (!currentUser) {
                return;
            }
            const updated = qty <= 0
                ? await api("/api/cart/me/items/" + itemId, { method: "DELETE" })
                : await api("/api/cart/me/items/" + itemId, { method: "PUT", body: JSON.stringify({ quantity: qty }) });
            setCart(updated);
        }

        async function sendChat() {
            if (!chatText.trim()) {
                return;
            }
            if (!currentUser) {
                setAuthMode("login");
                setAuthOpen(true);
                return;
            }
            const response = await api("/api/chat", { method: "POST", body: JSON.stringify({ message: chatText }) });
            setChatState(response);
            setChatText("");
            setChatOpen(true);
        }

        async function submitAuth() {
            const endpoint = authMode === "login" ? "/api/auth/login" : "/api/auth/register";
            const payload = authMode === "login"
                ? { email: authForm.email, password: authForm.password }
                : authForm;
            try {
                const user = await api(endpoint, { method: "POST", body: JSON.stringify(payload) });
                setCurrentUser(user);
                setAuthOpen(false);
                setAuthForm({ fullName: "", email: "", password: "", phone: "" });
            } catch (e) {
                window.alert("Dang nhap/ dang ky that bai: " + e.message);
            }
        }

        async function logout() {
            try {
                await fetch("/api/auth/logout", { method: "POST" });
            } finally {
                setCurrentUser(null);
                setDrawer(false);
            }
        }

        async function checkout() {
            if (!currentUser) {
                setAuthMode("login");
                setAuthOpen(true);
                return;
            }
            const defaultAddress = addresses.find(function (item) { return item.defaultAddress; }) || addresses[0];
            const shippingAddress = defaultAddress
                ? [defaultAddress.line1, defaultAddress.district, defaultAddress.city].filter(Boolean).join(", ")
                : "Dia chi giao hang chua cap nhat";
            try {
                const result = await api("/api/checkout/me", {
                    method: "POST",
                    body: JSON.stringify({
                        userId: currentUser.id,
                        paymentMethod: "COD",
                        shippingAddress: shippingAddress,
                        shippingProvider: "Noi bo"
                    })
                });
                window.alert("Tao don hang thanh cong. Ma don: " + result.orderId);
                await loadCart();
            } catch (e) {
                window.alert("Thanh toan that bai: " + e.message);
            }
        }

        const cartCount = useMemo(function () {
            return (cart.items || []).reduce(function (sum, item) { return sum + item.quantity; }, 0);
        }, [cart]);

        return h("div", null,
            h(Header, {
                cartCount: cartCount,
                filters: filters,
                setFilters: setFilters,
                categories: categories,
                currentUser: currentUser,
                openCart: function () { setDrawer(true); },
                openAuth: function () { setAuthMode("login"); setAuthOpen(true); },
                logout: logout
            }),
            route.page === "home"
                ? h(Home, {
                    loading: loading,
                    error: error,
                    products: products,
                    reco: reco,
                    filters: filters,
                    setFilters: setFilters,
                    categories: categories,
                    addToCart: addToCart
                })
                : h(DetailPage, {
                    loading: loading,
                    error: error,
                    detail: detail,
                    reviews: reviews,
                    heroImage: heroImage,
                    setHeroImage: setHeroImage,
                    addToCart: addToCart,
                    reco: reco,
                    categories: categories
                }),
            h("footer", { className: "footer-band" },
                h("div", { className: "footer-grid" },
                    h("div", { className: "footer-block" },
                        h("span", { className: "eyebrow" }, "Lien he"),
                        h("h3", { className: "footer-title" }, "Triple T Furniture"),
                        h("p", { className: "subtle" }, "Tu van noi that, combo phong mau va ho tro dat hang moi ngay.")
                    ),
                    h("div", { className: "footer-block" },
                        h("strong", null, "Showroom"),
                        h("p", null, "12 Nguyen Van Linh, Quan 7, TP. Ho Chi Minh"),
                        h("p", null, "Tang 2, khu trung bay phong khach va phong ngu")
                    ),
                    h("div", { className: "footer-block" },
                        h("strong", null, "Thong tin"),
                        h("p", null, "Hotline: 0868 066 109"),
                        h("p", null, "Email: trucgolden@gmail.com"),
                        h("p", null, "08:00 - 21:00, Thu 2 den Chu nhat")
                    ),
                    h("div", { className: "footer-block" },
                        h("strong", null, "Ho tro"),
                        h("p", null, "Chatbox AI 24/7"),
                        h("p", null, "Van chuyen noi that tai nha"),
                        h("p", null, "Bao hanh 5 nam cho san pham chon loc")
                    )
                )
            ),
            drawer ? h(CartDrawer, { cart: cart, currentUser: currentUser, close: function () { setDrawer(false); }, changeQty: changeQty, checkout: checkout }) : null,
            authOpen ? h(AuthModal, { mode: authMode, form: authForm, setForm: setAuthForm, setMode: setAuthMode, close: function () { setAuthOpen(false); }, submit: submitAuth }) : null,
            chatOpen ? h(ChatBox, { chatState: chatState, chatText: chatText, setChatText: setChatText, close: function () { setChatOpen(false); }, send: sendChat }) : null,
            h("div", { className: "chat-toggle" },
                h("button", { className: "pill-btn", type: "button", onClick: function () { setChatOpen(!chatOpen); } }, chatOpen ? "Dong tu van" : "Chat voi AI")
            )
        );
    }

    function Header(props) {
        const categoryMap = createCategoryMap(props.categories);
        const navCategories = props.categories.length
            ? props.categories
            : Object.keys(DEFAULT_CATEGORIES).map(function (code) { return { code: code, name: DEFAULT_CATEGORIES[code] }; });
        return h(React.Fragment, null,
            h("div", { className: "topbar" },
                h("div", { className: "topbar__left" }, h("span", null, "0868066109"), h("span", null, "trucgolden@gmail.com")),
                h("div", { className: "topbar__right" },
                    h("span", null, "Bao hanh 5 nam"),
                    props.currentUser
                        ? h(React.Fragment, null,
                            h("span", null, props.currentUser.fullName + " (" + props.currentUser.role + ")"),
                            props.currentUser.role === "ADMIN" ? h("a", { className: "ghost-btn", href: "/api/admin/dashboard", target: "_blank" }, "Admin") : null,
                            h("button", { className: "ghost-btn", type: "button", onClick: props.logout }, "Dang xuat")
                        )
                        : h("button", { className: "ghost-btn", type: "button", onClick: props.openAuth }, "Dang nhap"),
                    h("button", { className: "ghost-btn", type: "button", onClick: props.openCart }, "Gio hang (" + props.cartCount + ")")
                )
            ),
            h("div", { className: "brandbar" },
                h("a", { className: "brand", href: "/", onClick: function (e) { e.preventDefault(); go("/"); } }, h("span", { className: "brand__name" }, "TRIPLE T FURNITURE"), h("span", { className: "brand__tag" }, "Noi that song toi gian")),
                h("div", { className: "searchbar" },
                    h("input", { value: props.filters.keyword, placeholder: "Tim sofa, ban an, giuong, tu ...", onChange: function (e) { props.setFilters({ ...props.filters, keyword: e.target.value }); } }),
                    h("button", { className: "pill-btn", type: "button" }, "Kham pha")
                ),
                h("div", { className: "topbar__right" }, h("span", null, "Flash sale"), h("span", null, "AR room view"), h("span", null, "COD / Momo / ZaloPay"))
            ),
            h("div", { className: "mainnav" },
                navCategories.map(function (category) {
                    return h("button", {
                        key: category.code,
                        type: "button",
                        className: "nav-link" + (props.filters.category === category.code ? " is-active" : ""),
                        onClick: function () { props.setFilters({ ...props.filters, category: props.filters.category === category.code ? "" : category.code }); }
                    }, categoryMap[category.code] || category.name);
                })
            )
        );
    }

    function Home(props) {
        return h(React.Fragment, null,
            h("section", { className: "hero" },
                h("div", { className: "hero__content" }, h("div", { className: "hero__copy" },
                    h("span", { className: "eyebrow" }, "React storefront"),
                    h("h1", { className: "hero__headline" }, "Noi that hien dai cho khong gian song am va sach."),
                    h("p", null, "Catalog, detail, gio hang va chatbox hien duoc render boi React va lay du lieu truc tiep tu API."),
                    h("div", { className: "hero__actions" }, h("button", { className: "pill-btn", type: "button", onClick: function () { props.setFilters({ ...props.filters, category: "SOFA" }); } }, "Kham pha sofa"), h("button", { className: "ghost-btn", type: "button", onClick: function () { props.setFilters({ ...props.filters, category: "BED" }); } }, "Xem phong ngu"))
                )),
                h("aside", { className: "hero__panel" },
                    stat("Ban chay", "320+", "Combo phong an tu du lieu mau."),
                    stat("Thanh toan", "4 cach", "COD, chuyen khoan, Momo, ZaloPay."),
                    stat("Ho tro", "24/7", "Chatbox AI va nguoi that co the mo rong.")
                )
            ),
            h("section", { className: "section" },
                h("div", { className: "section-head" }, h("div", null, h("span", { className: "eyebrow" }, "Theo khong gian"), h("h2", { className: "section-title" }, "Lua chon theo phong"))),
                h("div", { className: "room-grid" }, ROOMS.map(function (room) {
                    return h("button", {
                        key: room[0],
                        className: "room-grid__item",
                        type: "button",
                        style: { backgroundImage: "linear-gradient(180deg, rgba(20,11,7,.1), rgba(20,11,7,.72)), url('" + room[2] + "')" },
                        onClick: function () { props.setFilters({ ...props.filters, category: room[1] }); }
                    }, h("div", null, h("h3", null, room[0]), h("p", null, "Xem danh muc phu hop.")));
                }))
            ),
            h("section", { className: "section catalog-layout" },
                h(FilterPanel, { filters: props.filters, setFilters: props.setFilters, categories: props.categories }),
                h("div", null,
                    h("div", { className: "collection-banner" },
                        h("div", { className: "hero__actions" }, h("span", { className: "ghost-btn" }, "Best seller"), h("span", { className: "ghost-btn" }, "Combo"), h("span", { className: "ghost-btn" }, "Review"))
                    ),
                    props.error ? h("div", { className: "empty-state" }, h("p", null, props.error)) : null,
                    props.loading ? h("div", { className: "empty-state" }, h("p", null, "Dang tai san pham...")) : renderProductGrid(props.products, props.addToCart)
                )
            ),
            h("section", { className: "section" },
                h("div", { className: "section-head" }, h("div", null, h("span", { className: "eyebrow" }, "Goi y thong minh"), h("h2", { className: "section-title" }, "De xuat cho ban"))),
                renderProductGrid((props.reco || []).slice(0, 4), props.addToCart)
            ),
            h("section", { className: "section showcase-grid" },
                showcase("Xem san pham trong khong gian thuc", "San sang mo rong tinh nang AR va combo theo dien tich phong.", "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=1400&q=80"),
                showcase("Don hang va marketing", "Flash sale, ma giam gia, email nhac gio hang va theo doi giao hang.", "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1400&q=80")
            )
        );
    }

    function FilterPanel(props) {
        function update(key, value) { props.setFilters({ ...props.filters, [key]: value }); }
        const selectCategories = props.categories.length
            ? props.categories
            : Object.keys(DEFAULT_CATEGORIES).map(function (code) { return { code: code, name: DEFAULT_CATEGORIES[code] }; });
        return h("aside", { className: "filter-panel" },
            h("span", { className: "eyebrow" }, "Bo loc"),
            h("h3", null, "Tinh chinh catalog"),
            field("Danh muc", h("select", { value: props.filters.category, onChange: function (e) { update("category", e.target.value); } }, h("option", { value: "" }, "Tat ca"), ...selectCategories.map(function (category) { return h("option", { key: category.code, value: category.code }, category.name); }))),
            field("Chat lieu", h("input", { value: props.filters.material, placeholder: "go, kim loai...", onChange: function (e) { update("material", e.target.value); } })),
            field("Mau sac", h("input", { value: props.filters.color, placeholder: "kem, nau, xam...", onChange: function (e) { update("color", e.target.value); } })),
            field("Kich thuoc", h("input", { value: props.filters.sizeLabel, placeholder: "220x90cm, 1m8 x 2m...", onChange: function (e) { update("sizeLabel", e.target.value); } })),
            field("Gia tu", h("input", { type: "number", value: props.filters.minPrice, onChange: function (e) { update("minPrice", e.target.value); } })),
            field("Gia den", h("input", { type: "number", value: props.filters.maxPrice, onChange: function (e) { update("maxPrice", e.target.value); } }))
        );
    }

    function DetailPage(props) {
        if (props.loading) { return h("section", { className: "section" }, h("div", { className: "empty-state" }, h("p", null, "Dang tai chi tiet san pham..."))); }
        if (props.error) { return h("section", { className: "section" }, h("div", { className: "empty-state" }, h("p", null, props.error))); }
        if (!props.detail) { return h("section", { className: "section" }, h("div", { className: "empty-state" }, h("p", null, "Khong tim thay san pham."))); }

        const d = props.detail;
        const img = props.heroImage || (d.images || [])[0];
        return h(React.Fragment, null,
            h("section", { className: "section" }, h("button", { className: "ghost-btn", type: "button", onClick: function () { go("/"); } }, "Quay lai catalog")),
            h("section", { className: "section detail-grid" },
                h("div", { className: "detail-grid__main" },
                    h("div", { className: "gallery-main" }, h("img", { src: img, alt: d.name })),
                    h("div", { className: "gallery-strip" }, (d.images || []).map(function (item) {
                        return h("button", { key: item, type: "button", onClick: function () { props.setHeroImage(item); } }, h("img", { src: item, alt: d.name }));
                    }))
                ),
                h("aside", { className: "detail-grid__aside" },
                    h("span", { className: "eyebrow" }, d.categoryName || createCategoryMap(props.categories)[d.categoryCode] || d.categoryCode),
                    h("h1", { className: "section-title" }, d.name),
                    h("div", { className: "detail-price" }, h("strong", null, money(d.promotionPrice || d.price)), d.promotionPrice ? h("span", null, money(d.price)) : null),
                    h("p", { className: "detail-note" }, d.description),
                    h("div", { className: "detail-specs" },
                        detailStat("Chat lieu", d.material), detailStat("Mau sac", d.color), detailStat("Kich thuoc", d.sizeLabel),
                        detailStat("Thong so", [d.lengthCm, d.widthCm, d.heightCm].filter(Boolean).join(" x ") + " cm"),
                        detailStat("Ton kho", String(d.stockQuantity || 0)), detailStat("Danh gia", String(d.averageRating || 0) + " / 5")
                    ),
                    h("div", { className: "stat-card" }, h("span", { className: "eyebrow" }, "Combo goi y"), h("p", { className: "subtle" }, d.comboSuggestion || "Ket hop voi tham, den cay va ban phu.")),
                    d.arEnabled ? h("div", { className: "stat-card" }, h("span", { className: "eyebrow" }, "AR"), h("p", { className: "subtle" }, "San pham nay san sang cho demo xem trong phong.")) : null,
                    h("button", { className: "pill-btn", type: "button", onClick: function () { props.addToCart(d.id); } }, "Them vao gio hang")
                )
            ),
            h("section", { className: "section" }, h("div", { className: "section-head" }, h("div", null, h("span", { className: "eyebrow" }, "Danh gia"), h("h2", { className: "section-title" }, "Phan hoi thuc te"))), renderReviews(props.reviews)),
            h("section", { className: "section" }, h("div", { className: "section-head" }, h("div", null, h("span", { className: "eyebrow" }, "Lien quan"), h("h2", { className: "section-title" }, "Ban co the cung thich"))), renderProductGrid((props.reco || []).slice(0, 4), props.addToCart))
        );
    }

    function CartDrawer(props) {
        return h(React.Fragment, null,
            h("div", { className: "drawer-backdrop", onClick: props.close }),
            h("aside", { className: "drawer" },
                h("div", { className: "drawer__head" }, h("h3", null, "Gio hang"), h("button", { className: "ghost-btn", type: "button", onClick: props.close }, "Dong")),
                h("div", { className: "drawer__body" }, (props.cart.items || []).length ? props.cart.items.map(function (item) {
                    return h("article", { className: "cart-row", key: item.itemId },
                        h("img", { src: item.imageUrl, alt: item.productName }),
                        h("div", null,
                            h("div", { className: "cart-row__meta" }, h("strong", null, item.productName), h("span", null, money(item.lineTotal))),
                            h("p", { className: "subtle" }, money(item.unitPrice)),
                            h("div", { className: "quantity-row" },
                                h("button", { type: "button", onClick: function () { props.changeQty(item.itemId, item.quantity - 1); } }, "-"),
                                h("button", { type: "button" }, String(item.quantity)),
                                h("button", { type: "button", onClick: function () { props.changeQty(item.itemId, item.quantity + 1); } }, "+")
                            )
                        )
                    );
                }) : h("div", { className: "empty-state" }, h("p", null, "Gio hang hien dang trong."))),
                h("div", null,
                    h("div", { className: "section-head" }, h("strong", null, "Tong tien"), h("strong", null, money(props.cart.totalAmount))),
                    h("button", { className: "pill-btn", type: "button", onClick: props.checkout }, props.currentUser ? "Thanh toan" : "Dang nhap de thanh toan")
                )
            )
        );
    }

    function AuthModal(props) {
        function update(key, value) {
            props.setForm({ ...props.form, [key]: value });
        }

        return h(React.Fragment, null,
            h("div", { className: "drawer-backdrop", onClick: props.close }),
            h("aside", { className: "drawer drawer--auth" },
                h("div", { className: "drawer__head" },
                    h("h3", null, props.mode === "login" ? "Dang nhap" : "Dang ky"),
                    h("button", { className: "ghost-btn", type: "button", onClick: props.close }, "Dong")
                ),
                h("div", { className: "drawer__body drawer__body--auth" },
                    props.mode === "register" ? field("Ho ten", h("input", { value: props.form.fullName, onChange: function (e) { update("fullName", e.target.value); } })) : null,
                    field("Email", h("input", { value: props.form.email, onChange: function (e) { update("email", e.target.value); } })),
                    field("Mat khau", h("input", { type: "password", value: props.form.password, onChange: function (e) { update("password", e.target.value); } })),
                    props.mode === "register" ? field("So dien thoai", h("input", { value: props.form.phone, onChange: function (e) { update("phone", e.target.value); } })) : null
                ),
                h("div", null,
                    h("div", { className: "chatbox__actions" },
                        h("button", {
                            className: "ghost-btn",
                            type: "button",
                            onClick: function () { props.setMode(props.mode === "login" ? "register" : "login"); }
                        }, props.mode === "login" ? "Chuyen sang dang ky" : "Chuyen sang dang nhap"),
                        h("button", { className: "pill-btn", type: "button", onClick: props.submit }, props.mode === "login" ? "Dang nhap" : "Dang ky")
                    )
                )
            )
        );
    }

    function ChatBox(props) {
        function onKeyDown(event) {
            if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();
                props.send();
            }
        }

        return h("aside", { className: "chatbox" },
            h("div", { className: "chatbox__header" }, h("div", null, h("strong", null, props.chatState.assistantName || "Noi That Assistant"), h("div", { className: "subtle" }, "Tu van nhanh ve noi that")), h("button", { className: "ghost-btn", type: "button", onClick: props.close }, "Dong")),
            h("div", { className: "chatbox__history" }, (props.chatState.history || []).length ? props.chatState.history.map(function (msg, index) {
                return h("div", { key: String(index) + msg.createdAt, className: "chat-msg " + (msg.sender === "USER" ? "user" : "ai") }, h("div", null, msg.content), h("time", null, new Date(msg.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })));
            }) : h("div", { className: "empty-state" }, h("p", null, "Hoi ve sofa, giuong, combo noi that hoac mau sac phu hop."))),
            h("div", null,
                h("textarea", { rows: 3, value: props.chatText, placeholder: "Toi can tu van sofa cho phong khach nho...", onChange: function (e) { props.setChatText(e.target.value); }, onKeyDown: onKeyDown }),
                h("div", { className: "chatbox__actions" }, h("button", { className: "ghost-btn", type: "button", onClick: function () { props.setChatText("Tu van sofa cho phong khach nho"); } }, "Goi y sofa"), h("button", { className: "pill-btn", type: "button", onClick: props.send }, "Gui"))
            )
        );
    }

    function stat(label, value, text) { return h("div", { className: "stat-card" }, h("span", { className: "eyebrow" }, label), h("strong", null, value), h("p", { className: "subtle" }, text)); }
    function field(label, control) { return h("div", { className: "filter-group" }, h("label", null, label), control); }
    function detailStat(label, value) { return h("div", { className: "detail-stat" }, h("span", null, label), h("strong", null, value)); }
    function showcase(title, description, image) { return h("div", { className: "showcase-card", style: { backgroundImage: "url('" + image + "')" } }, h("div", { className: "showcase-card__overlay" }, h("span", { className: "eyebrow" }, "Noi bat"), h("h3", null, title), h("p", null, description))); }

    function renderProductGrid(products, addToCart) {
        return products && products.length ? h("div", { className: "product-grid" }, products.map(function (product) {
            return h("article", { className: "product-card", key: product.id },
                h("a", { href: "/products/" + product.slug, onClick: function (e) { e.preventDefault(); go("/products/" + product.slug); } }, h("img", { src: product.primaryImage, alt: product.name })),
                h("div", { className: "product-card__body" },
                    h("div", { className: "product-card__meta" }, h("span", null, product.categoryName || product.categoryCode), h("span", null, product.status === "IN_STOCK" ? "Con hang" : "Het hang")),
                    h("a", { href: "/products/" + product.slug, onClick: function (e) { e.preventDefault(); go("/products/" + product.slug); } }, h("h3", { className: "product-card__title" }, product.name)),
                    h("p", { className: "subtle" }, [product.material, product.color, product.sizeLabel].filter(Boolean).join(" / ")),
                    h("div", { className: "price-block" }, h("strong", null, money(product.promotionPrice || product.price)), product.promotionPrice ? h("span", null, money(product.price)) : null),
                    h("div", { className: "product-card__foot" }, h("span", { className: "stock" }, product.status === "IN_STOCK" ? "San sang giao" : "Tam het"), h("button", { className: "pill-btn", type: "button", disabled: product.status !== "IN_STOCK", onClick: function () { addToCart(product.id); } }, "Them vao gio"))
                )
            );
        })) : h("div", { className: "empty-state" }, h("p", null, "Khong co san pham phu hop voi bo loc hien tai."));
    }

    function renderReviews(reviews) {
        return reviews && reviews.length ? h("div", { className: "review-grid" }, reviews.map(function (review, index) {
            return h("article", { className: "review-card", key: review.userName + index }, h("div", { className: "review-card__meta" }, h("strong", null, review.userName), h("span", null, review.rating + " sao")), h("p", null, review.comment), review.imageUrl ? h("img", { src: review.imageUrl, alt: review.userName }) : null);
        })) : h("div", { className: "empty-state" }, h("p", null, "Chua co danh gia nao cho san pham nay."));
    }

    function createCategoryMap(categories) {
        return (categories || []).reduce(function (map, category) {
            map[category.code] = category.name;
            return map;
        }, { ...DEFAULT_CATEGORIES });
    }
    ReactDOM.createRoot(document.getElementById("root")).render(h(App));
})();
