import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class RestaurantBillingApp {
    private static final String PAGE = """
            <!doctype html>
            <html>
            <head>
                <title>Restaurant Bill</title>
                <style>
                    :root {
                        color-scheme: light;
                        font-family: "Segoe UI", Arial, sans-serif;
                        color: #24352f;
                        background: #f5f2e9;
                        --green: #245c45;
                        --green-dark: #194533;
                        --muted: #78827b;
                        --border: #e6e9e2;
                    }
                    * { box-sizing: border-box; }
                    body {
                        min-height: 100vh;
                        margin: 0;
                        padding: 48px 20px;
                        display: grid;
                        place-items: center;
                        background:
                            radial-gradient(ellipse at 10% 10%, #e8eddd 0, transparent 35%),
                            radial-gradient(ellipse at 95% 90%, #f1e4d3 0, transparent 32%),
                            #f5f2e9;
                    }
                    main {
                        width: min(100%, 560px);
                        padding: 38px;
                        border: 1px solid rgba(255, 255, 255, 0.8);
                        border-radius: 24px;
                        background: rgba(255, 255, 255, 0.94);
                        box-shadow: 0 24px 70px rgba(37, 55, 42, 0.12);
                    }
                    .eyebrow {
                        margin: 0 0 8px;
                        color: #b06b3d;
                        font-size: 0.75rem;
                        font-weight: 800;
                        letter-spacing: 0.16em;
                        text-transform: uppercase;
                    }
                    h1 {
                        margin: 0;
                        color: #203d31;
                        font-family: Georgia, "Times New Roman", serif;
                        font-size: clamp(2rem, 6vw, 2.7rem);
                        letter-spacing: -0.04em;
                    }
                    .intro { margin: 10px 0 28px; color: var(--muted); line-height: 1.6; }
                    .menu-grid { display: grid; gap: 12px; }
                    .menu-item {
                        padding: 16px;
                        border: 1px solid var(--border);
                        border-radius: 15px;
                        background: #fff;
                        transition: border-color 160ms ease, box-shadow 160ms ease;
                    }
                    .menu-item:focus-within {
                        border-color: #9db9a4;
                        box-shadow: 0 0 0 3px rgba(36, 92, 69, 0.08);
                    }
                    .menu-item label {
                        display: block;
                        margin-bottom: 10px;
                        color: #30453b;
                        font-size: 0.92rem;
                        font-weight: 700;
                    }
                    .controls { display: grid; grid-template-columns: minmax(0, 1fr) 112px; gap: 10px; }
                    select, button { min-height: 46px; border-radius: 10px; font: inherit; }
                    select {
                        width: 100%;
                        padding: 0 12px;
                        border: 1px solid #dfe4dc;
                        background-color: #fbfcfa;
                        color: #34443b;
                        cursor: pointer;
                    }
                    select:focus-visible, button:focus-visible {
                        outline: 3px solid rgba(36, 92, 69, 0.25);
                        outline-offset: 2px;
                    }
                    button {
                        width: 100%;
                        margin-top: 20px;
                        padding: 0 18px;
                        border: 0;
                        background: var(--green);
                        color: #fff;
                        font-weight: 700;
                        cursor: pointer;
                        transition: background 160ms ease, transform 160ms ease;
                    }
                    button:hover { background: var(--green-dark); transform: translateY(-1px); }
                    #total {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin: 18px 0 0;
                        padding: 18px 20px;
                        border-radius: 14px;
                        background: #edf4ed;
                        color: var(--green-dark);
                        font-size: 1.12rem;
                    }
                    @media (max-width: 480px) {
                        body { padding: 20px 12px; }
                        main { padding: 26px 20px; border-radius: 20px; }
                        .menu-item { padding: 13px; }
                        .controls { grid-template-columns: minmax(0, 1fr) 88px; gap: 8px; }
                    }
                </style>
            </head>
            <body>
                <main>
                <p class="eyebrow">Freshly prepared, simply priced</p>
                <h1>Restaurant Billing</h1>
                <p class="intro">Choose your favorites and quantities to see your bill.</p>

                <div class="menu-grid">
                    <section class="menu-item">
                        <label for="food">Food</label>
                        <div class="controls">
                            <select id="food">
                                <option value="0">Select food</option>
                                <option value="8.50">Burger - $8.50</option>
                                <option value="12">Pizza - $12.00</option>
                                <option value="10">Pasta - $10.00</option>
                            </select>
                            <select id="foodQuantity" aria-label="Food quantity">
                                <option value="1">1</option><option value="2">2</option><option value="3">3</option>
                            </select>
                        </div>
                    </section>

                    <section class="menu-item">
                        <label for="drink">Drink</label>
                        <div class="controls">
                            <select id="drink">
                                <option value="0">Select drink</option>
                                <option value="1.50">Water - $1.50</option>
                                <option value="3">Juice - $3.00</option>
                                <option value="2.50">Coffee - $2.50</option>
                            </select>
                            <select id="drinkQuantity" aria-label="Drink quantity">
                                <option value="1">1</option><option value="2">2</option><option value="3">3</option>
                            </select>
                        </div>
                    </section>

                    <section class="menu-item">
                        <label for="snack">Snack</label>
                        <div class="controls">
                            <select id="snack">
                                <option value="0">Select snack</option>
                                <option value="3.50">Fries - $3.50</option>
                                <option value="5">Nachos - $5.00</option>
                                <option value="6">Sandwich - $6.00</option>
                            </select>
                            <select id="snackQuantity" aria-label="Snack quantity">
                                <option value="1">1</option><option value="2">2</option><option value="3">3</option>
                            </select>
                        </div>
                    </section>

                    <section class="menu-item">
                        <label for="dessert">Dessert</label>
                        <div class="controls">
                            <select id="dessert">
                                <option value="0">Select dessert</option>
                                <option value="4">Ice cream - $4.00</option>
                                <option value="4.50">Cake - $4.50</option>
                                <option value="5">Brownie - $5.00</option>
                            </select>
                            <select id="dessertQuantity" aria-label="Dessert quantity">
                                <option value="1">1</option><option value="2">2</option><option value="3">3</option>
                            </select>
                        </div>
                    </section>
                </div>

                <button type="button" onclick="calculateTotal()">Calculate total</button>
                <h2 id="total">Total: $0.00</h2>
                </main>

                <script>
                    function calculateTotal() {
                        let total = 0;
                        total += Number(document.getElementById("food").value)
                            * Number(document.getElementById("foodQuantity").value);
                        total += Number(document.getElementById("drink").value)
                            * Number(document.getElementById("drinkQuantity").value);
                        total += Number(document.getElementById("snack").value)
                            * Number(document.getElementById("snackQuantity").value);
                        total += Number(document.getElementById("dessert").value)
                            * Number(document.getElementById("dessertQuantity").value);
                        document.getElementById("total").textContent =
                            "Total: $" + total.toFixed(2);
                    }
                </script>
            </body>
            </html>
            """;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8081), 0);

        server.createContext("/", exchange -> {
            byte[] response = PAGE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.start();
        System.out.println("Open http://localhost:8081 in your browser.");
    }
}
