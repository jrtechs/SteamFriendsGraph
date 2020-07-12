function profileGen(data, container)
{
    let html = `
        <div class="card shadow-sm" style="font-size: 16px;">
            <div class="card-img-top" style="position: relative;">
                <img src="${data.avatar}" alt="${data.avatar}" width="100%" class="img-fluid"/>
            </div>
            <div class="card-body">
                ${data.name ? `<h5 class="card-title mb-1">${data.name}</h5>` : ""}
                <a href="${data.avatar}" class="card-subtitle text-muted">${data.realName}</a>
                <ul class="list-unstyled">
                    <li><a href="https://steamcommunity.com/profiles/${data.id}">Steam profile</a></li>
                    <li><a href="/friendsGraph.html?id=${data.id}">Friends Graph</a></li>
                </ul>
            </div>
        </div>
    `;
    $("#"+container).html(html);
}