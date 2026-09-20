using UnityEngine;

public enum PopupTransitionType
{
    ItemInfo,
    Reward
}

/// <summary>
/// Hiệu ứng dùng chung cho popup. Chức năng mới chỉ cần tạo một instance,
/// gọi Show/Close, Update mỗi frame và bọc phần Paint bằng PushOpacity/PopOpacity.
/// </summary>
public sealed class PopupTransition
{
    private const float OpenDuration = 0.5f;
    private readonly PopupTransitionType type;
    private float time;
    private bool closing;

    public bool Active { get; private set; }

    public bool IsClosing { get { return Active && closing; } }

    public PopupTransition(PopupTransitionType type)
    {
        this.type = type;
    }

    public void Show()
    {
        Active = true;
        closing = false;
        time = 0f;
    }

    public void Close()
    {
        if (!Active || closing) return;
        closing = true;
        time = 0f;
    }

    public void CloseImmediately()
    {
        Active = false;
        closing = false;
        time = 0f;
    }

    public void Update(float deltaTime)
    {
        if (!Active) return;
        time += Mathf.Max(0f, deltaTime);
        if (closing && time >= CloseDuration)
        {
            CloseImmediately();
        }
    }

    public float Opacity
    {
        get
        {
            if (!Active) return 0f;
            float progress = Mathf.Clamp01(time / (closing ? CloseDuration : OpenDuration));
            return closing ? 1f - EaseInOut(progress) : EaseOut(progress);
        }
    }

    public int VerticalOffset
    {
        get
        {
            if (!Active || closing || type != PopupTransitionType.Reward) return 0;
            float progress = EaseOutBack(Mathf.Clamp01(time / OpenDuration));
            return Mathf.RoundToInt((GameCanvas.h * 0.35f) * (1f - progress));
        }
    }

    public float Scale
    {
        get
        {
            if (!Active || closing || type != PopupTransitionType.ItemInfo) return 1f;
            float progress = EaseOutBack(Mathf.Clamp01(time / OpenDuration));
            return 0.82f + 0.18f * progress;
        }
    }

    public void PaintDimBackground(mGraphics g, float maximumOpacity)
    {
        float opacity = maximumOpacity * Opacity;
        if (opacity <= 0f) return;
        g.setColor(0, opacity);
        g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    }

    public void TransformBounds(ref int x, ref int y, ref int width, ref int height)
    {
        y += VerticalOffset;
    }

    public Matrix4x4 PushScale(int centerX, int centerY)
    {
        Matrix4x4 previous = GUI.matrix;
        float scale = Scale;
        if (System.Math.Abs(scale - 1f) > 0.001f)
        {
            Vector2 pivot = new Vector2(centerX * mGraphics.zoomLevel, centerY * mGraphics.zoomLevel);
            GUIUtility.ScaleAroundPivot(new Vector2(scale, scale), pivot);
        }
        return previous;
    }

    public void PopScale(Matrix4x4 previous)
    {
        GUI.matrix = previous;
    }

    private float CloseDuration
    {
        get { return type == PopupTransitionType.ItemInfo ? 0.3f : 0.2f; }
    }

    private static float EaseOut(float value)
    {
        float inverse = 1f - value;
        return 1f - inverse * inverse * inverse;
    }

    private static float EaseInOut(float value)
    {
        return value < 0.5f ? 2f * value * value : 1f - Mathf.Pow(-2f * value + 2f, 2f) / 2f;
    }

    private static float EaseOutBack(float value)
    {
        const float c1 = 1.70158f;
        const float c3 = c1 + 1f;
        float shifted = value - 1f;
        return 1f + c3 * shifted * shifted * shifted + c1 * shifted * shifted;
    }
}
