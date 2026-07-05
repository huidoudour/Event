// Event 应用页面交互脚本

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 设置当前日期
    const now = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    const currentDateElement = document.getElementById('currentDate');
    if (currentDateElement) {
        currentDateElement.textContent = now.toLocaleDateString('zh-CN', options);
    }
    
    // 平滑滚动
    initSmoothScroll();
    
    // 导航栏滚动效果
    initNavbarScroll();
    
    // 添加滚动动画
    initScrollAnimations();
    
    // 初始化返回顶部按钮
    initBackToTopButton();
});

// 平滑滚动
function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// 导航栏滚动效果
function initNavbarScroll() {
    const navbar = document.querySelector('.navbar');
    let lastScroll = 0;
    
    window.addEventListener('scroll', () => {
        const currentScroll = window.pageYOffset;
        
        if (currentScroll > 100) {
            navbar.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.boxShadow = '0 5px 15px rgba(0, 0, 0, 0.08)';
        }
        
        lastScroll = currentScroll;
    });
}

// 滚动动画
function initScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // 观察功能卡片
    document.querySelectorAll('.feature-card').forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = `all 0.6s ease ${index * 0.1}s`;
        observer.observe(card);
    });
    
    // 观察技术栈项目
    document.querySelectorAll('.tech-item').forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(30px)';
        item.style.transition = `all 0.6s ease ${index * 0.05}s`;
        observer.observe(item);
    });
    
    // 观察统计信息卡片
    document.querySelectorAll('.stat-item').forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(30px)';
        item.style.transition = `all 0.6s ease ${index * 0.1}s`;
        observer.observe(item);
    });
}

// 初始化返回顶部按钮
function initBackToTopButton() {
    // 创建返回顶部按钮
    const backToTopBtn = document.createElement('div');
    backToTopBtn.className = 'back-to-top';
    backToTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
    backToTopBtn.title = '返回顶部';
    document.body.appendChild(backToTopBtn);
    
    // 监听滚动事件
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopBtn.classList.add('show');
        } else {
            backToTopBtn.classList.remove('show');
        }
    });
    
    // 点击返回顶部
    backToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

// 页面可见性变化时的标题变化效果
document.addEventListener('visibilitychange', function () {
    var isHidden = document.hidden;
    if (isHidden) {
        document.title = "Event|不要走嘛(*´д`*)";
    } else {
        setTimeout(() => {
            document.title = "Event|好耶,回来了(づ￣3￣)づ╭❤～";
        }, 1000);
        setTimeout(() => {
            document.title = "Event - 轻量级记事本应用";
        }, 2200);
    }
});

// 添加控制台欢迎信息
console.log("%c🌟 Event - 轻量级记事本应用", "font-size: 20px; font-weight: bold; color: #4facfe;");
console.log("%c📝 简洁高效，专注记录", "font-size: 14px; color: #00f2fe;");
console.log("%c💡 GitHub: https://github.com/huidoudour/Event", "font-size: 12px; color: #636e72;");
console.log("%c☕ 感谢访问，记得给个 Star 哦~", "font-size: 12px; color: #43e97b;");

// 添加鼠标点击特效
document.addEventListener('click', function(e) {
    // 只在非链接区域显示特效
    if (e.target.tagName !== 'A' && !e.target.closest('a')) {
        const heart = document.createElement('div');
        heart.innerHTML = '❤';
        heart.style.position = 'fixed';
        heart.style.left = (e.clientX - 10) + 'px';
        heart.style.top = (e.clientY - 20) + 'px';
        heart.style.fontSize = '20px';
        heart.style.color = '#ff6b6b';
        heart.style.pointerEvents = 'none';
        heart.style.zIndex = '9999';
        heart.style.userSelect = 'none';
        document.body.appendChild(heart);
        
        setTimeout(() => {
            heart.style.transition = 'all 1s ease-out';
            heart.style.opacity = '0';
            heart.style.transform = 'translateY(-30px)';
            setTimeout(() => {
                document.body.removeChild(heart);
            }, 1000);
        }, 10);
    }
});
